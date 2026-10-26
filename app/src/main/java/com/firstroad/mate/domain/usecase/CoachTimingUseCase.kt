package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.BeginnerLevel
import com.firstroad.mate.domain.model.CoachDecision
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.TurnSignal
import javax.inject.Inject

/**
 * ② 첫길 코치 · ③ 고속도로 리허설의 타이밍 판단.
 *
 * '선배식 미리 안내'는 일반 내비의 "300m 앞" 안내보다 항상 먼저 나와야 한다.
 * - 차선 준비: 회전 25초 전 또는 350m 전 중 먼저 오는 때 (시속 50km에서 약 350m),
 *   고속도로 합류·분기는 1km 전
 * - 7초 전: 방향지시등이 꺼져 있으면 부드럽게 알림
 * - 3초 전: 방향지시등 사용 여부를 기록하고, 켰으면 칭찬
 * 속도를 모르거나 매우 느리면 거리 기준으로 판단한다.
 */
class CoachTimingUseCase @Inject constructor() {

    operator fun invoke(
        turn: TurnGuide?,
        speedKmh: Float?,
        signal: TurnSignal,
        level: BeginnerLevel,
    ): List<CoachDecision> {
        if (turn == null) return emptyList()
        val required = turn.direction.requiredSignal() ?: return emptyList()
        val stage = stageOf(turn, speedKmh)
        val decisions = mutableListOf<CoachDecision>()

        if (stage >= Stage.PREPARE && (level.coachesSignalPreparation || turn.isHighwayJunction)) {
            decisions += CoachDecision(
                key = "${turn.id}:prepare",
                guidance = Guidance.PrepareLane(turn.direction, turn.distanceMeters, turn.isHighwayJunction),
            )
        }
        if (stage >= Stage.REMIND && signal != required) {
            decisions += CoachDecision(key = "${turn.id}:remind", guidance = Guidance.SignalReminder(turn.direction))
        }
        if (stage >= Stage.JUDGE) {
            val used = signal == required
            decisions += CoachDecision(
                key = "${turn.id}:judge",
                guidance = if (used && level.praisesOften) Guidance.SignalPraise else null,
                countsAsSignalUsed = used,
                countsAsSignalMissed = !used,
            )
        }
        return decisions
    }

    private fun stageOf(turn: TurnGuide, speedKmh: Float?): Stage {
        val distance = turn.distanceMeters
        val speed = speedKmh ?: 0f
        val secondsToTurn = if (speed < MIN_SPEED_FOR_TIME_KMH) null else distance / (speed / 3.6f)
        return when {
            distance <= JUDGE_DISTANCE_M || (secondsToTurn != null && secondsToTurn <= JUDGE_SECONDS) -> Stage.JUDGE
            distance <= REMIND_DISTANCE_M || (secondsToTurn != null && secondsToTurn <= REMIND_SECONDS) -> Stage.REMIND
            turn.isHighwayJunction && distance <= HIGHWAY_PREPARE_DISTANCE_M -> Stage.PREPARE
            distance <= PREPARE_DISTANCE_M || (secondsToTurn != null && secondsToTurn <= PREPARE_SECONDS) -> Stage.PREPARE
            else -> Stage.NONE
        }
    }

    private enum class Stage { NONE, PREPARE, REMIND, JUDGE }

    companion object {
        const val PREPARE_SECONDS = 25f
        const val REMIND_SECONDS = 7f
        const val JUDGE_SECONDS = 3f
        const val PREPARE_DISTANCE_M = 350
        const val HIGHWAY_PREPARE_DISTANCE_M = 1_000
        private const val MIN_SPEED_FOR_TIME_KMH = 5f
        private const val REMIND_DISTANCE_M = 60
        private const val JUDGE_DISTANCE_M = 25
    }
}
