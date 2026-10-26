package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.DriveEvent
import com.firstroad.mate.domain.model.DriveInput
import com.firstroad.mate.domain.model.DriveSessionState
import com.firstroad.mate.domain.model.DriveTick
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.PendingAction
import com.firstroad.mate.utils.GeoMath
import javax.inject.Inject

/**
 * 주행 중 판단을 한 곳에 모은 순수 리듀서: (이전 상태, 이번 입력) → (새 상태, 할 일).
 * ② 코칭 · ③ 리허설 브리핑 · ④ 경고 · ⑤ 긴장 완화 · 도착 판단을 차례로 수행한다.
 */
class DriveTickUseCase @Inject constructor(
    private val coachTiming: CoachTimingUseCase,
    private val detectHardBrake: DetectHardBrakeUseCase,
    private val classifyWarning: ClassifyWarningUseCase,
    private val tensionScore: TensionScoreUseCase,
    private val buildBriefing: BuildRouteBriefingUseCase,
) {
    operator fun invoke(previous: DriveSessionState, input: DriveInput): DriveTick {
        if (previous.isFinished) return DriveTick(previous, emptyList())
        var state = previous
        val events = mutableListOf<DriveEvent>()

        if (state.startedAtMillis == null) {
            if (!input.navigation.isRouteActive) return DriveTick(state, events)
            state = state.copy(startedAtMillis = input.nowMillis)
        }
        state = brief(state, input, events)
        state = coach(state, input, events)
        state = hardBrake(state, input, events)
        state = speedLimit(state, input, events)
        state = warnings(state, input, events)
        state = calmCabin(state, input, events)
        state = arrival(state, input, events)
        return DriveTick(state, events)
    }

    private fun brief(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        if (state.isBriefed) return state
        val destination = input.destination ?: input.location
        if (destination != null) {
            events += DriveEvent.Say(buildBriefing(input.navigation, input.difficultSpots, input.location, destination))
        }
        return state.copy(isBriefed = true)
    }

    private fun coach(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        var result = state
        coachTiming(input.navigation.nextTurn, input.vehicle.speedKmh, input.vehicle.turnSignal, input.level)
            .filter { it.key !in result.handledKeys }
            .forEach { decision ->
                val stats = result.stats.copy(
                    signalUsed = result.stats.signalUsed + if (decision.countsAsSignalUsed) 1 else 0,
                    signalMissed = result.stats.signalMissed + if (decision.countsAsSignalMissed) 1 else 0,
                )
                result = result.copy(handledKeys = result.handledKeys + decision.key, stats = stats)
                decision.guidance?.let { events += DriveEvent.Say(it) }
            }
        return result
    }

    private fun hardBrake(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        val speed = input.vehicle.speedKmh
        // 다른 신호(내비·위치) 때문에 생긴 틱은 차속 샘플이 아니므로 시각을 갱신하지 않는다 (급제동 오탐 방지).
        if (speed == state.lastSpeedKmh) return state
        val isHard = detectHardBrake(state.lastSpeedKmh, state.lastSpeedAtMillis, speed, input.nowMillis)
        // 한 번의 급제동이 여러 측정 구간에 걸쳐도 한 번으로 센다.
        val isNewBrake = state.lastHardBrakeAtMillis?.let { input.nowMillis - it > HARD_BRAKE_DEBOUNCE_MILLIS } ?: true
        var result = state.copy(lastSpeedKmh = speed, lastSpeedAtMillis = input.nowMillis)
        if (isHard && isNewBrake) {
            events += DriveEvent.Say(Guidance.HardBrakeTip)
            result = result.copy(
                stats = result.stats.copy(hardBrakes = result.stats.hardBrakes + 1),
                lastHardBrakeAtMillis = input.nowMillis,
            )
        }
        return result
    }

    private fun speedLimit(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        val limit = input.navigation.speedLimitKmh ?: return state
        val speed = input.vehicle.speedKmh ?: return state
        val key = "speed:$limit"
        if (speed <= limit + SPEED_MARGIN_KMH || key in state.handledKeys) return state
        events += DriveEvent.Say(Guidance.SpeedOver(limit))
        return state.copy(handledKeys = state.handledKeys + key)
    }

    private fun warnings(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        var result = state
        input.vehicle.warnings.filter { it.type !in result.seenWarnings }.forEach { warning ->
            val explanation = classifyWarning(warning)
            events += DriveEvent.ShowWarning(explanation)
            events += DriveEvent.Say(Guidance.WarningNotice(warning.type, explanation.level))
            explanation.suggestedStop?.let { stop ->
                events += DriveEvent.Ask(Guidance.AskAddWaypoint(stop), PendingAction.AddWaypoint(stop))
            }
            result = result.copy(seenWarnings = result.seenWarnings + warning.type)
        }
        return result
    }

    private fun calmCabin(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        val recentHardBrake = state.lastHardBrakeAtMillis?.let { input.nowMillis - it < RECENT_HARD_BRAKE_MILLIS } ?: false
        val nearSpot = input.location?.let { here ->
            input.difficultSpots.any { GeoMath.distanceMeters(here, it.location) <= NEAR_SPOT_METERS }
        } ?: false
        val score = tensionScore(input.navigation, input.vehicle, recentHardBrake, nearSpot)
        var result = state.copy(tension = score)
        if (!state.isCalmActive && tensionScore.adjustmentFor(score) != null) {
            events += DriveEvent.StartCalm
            result = result.copy(isCalmActive = true)
        } else if (state.isCalmActive && score < CALM_RELEASE_SCORE) {
            events += DriveEvent.EndCalm
            events += DriveEvent.Say(Guidance.PassedWell)
            result = result.copy(
                isCalmActive = false,
                stats = result.stats.copy(difficultPassed = result.stats.difficultPassed + 1),
            )
        }
        return result
    }

    private fun arrival(state: DriveSessionState, input: DriveInput, events: MutableList<DriveEvent>): DriveSessionState {
        var result = state
        if (input.navigation.isArrived && !result.isArrived) {
            events += DriveEvent.Say(Guidance.Arrived)
            result = result.copy(isArrived = true)
        }
        if (result.isArrived && input.vehicle.isParked) {
            val started = result.startedAtMillis ?: input.nowMillis
            val duration = ((input.nowMillis - started) / MILLIS_PER_SECOND).toInt()
            events += DriveEvent.Finished
            result = result.copy(isFinished = true, stats = result.stats.copy(durationSeconds = duration))
        }
        return result
    }

    private companion object {
        const val SPEED_MARGIN_KMH = 10
        const val RECENT_HARD_BRAKE_MILLIS = 10_000L
        const val HARD_BRAKE_DEBOUNCE_MILLIS = 3_000L
        const val NEAR_SPOT_METERS = 300.0
        const val CALM_RELEASE_SCORE = 30
        const val MILLIS_PER_SECOND = 1_000L
    }
}
