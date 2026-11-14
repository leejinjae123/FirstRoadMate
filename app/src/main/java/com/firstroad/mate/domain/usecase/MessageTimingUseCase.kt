package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.DriveInput
import com.firstroad.mate.domain.model.DriveSessionState
import com.firstroad.mate.domain.model.TurnDirection
import javax.inject.Inject

/**
 * 메시지를 지금 꺼내도 되는 순간인지 판단한다 (초보 운전자의 주의 분산을 줄이는 '타이밍 조절').
 * - 정차 중(신호 대기 등)이면 바로 괜찮다. 단 목적지 500m 안에서는 주차에 집중하게 묻지 않는다 (하차 화면에서 묻는다).
 * - 달리는 중에는 회전 준비 구간(일반 25초·350m, 고속도로 분기 1km), 주의 지점(합류·어린이보호구역 등) 500m 앞,
 *   진정 모드·긴장 지수 높음, 최근 급제동, 새 경고등이 있으면 보류한다.
 * - 보류할 일이 없는 '조용한 구간'이 몇 초 이어져야 꺼낸다 (회전 직후 바로 묻지 않게).
 */
class MessageTimingUseCase @Inject constructor() {

    /** 지금이 조용한 순간인지 (이어진 시간은 따지지 않음). */
    fun isQuietNow(state: DriveSessionState, input: DriveInput): Boolean {
        val speed = input.vehicle.speedKmh ?: return false
        if (input.vehicle.warnings.any { it.type !in state.seenWarnings }) return false
        if (state.isCalmActive || state.tension >= HOLD_TENSION) return false
        // 목적지 앞에서는 주차 자리 찾기에 집중하게 하고, 메시지는 하차 화면에서 묻는다.
        input.navigation.remainingMeters?.let { if (it <= ARRIVAL_HOLD_METERS) return false }
        if (speed <= STOPPED_KMH) return true
        val recentHardBrake = state.lastHardBrakeAtMillis?.let { input.nowMillis - it < RECENT_HARD_BRAKE_MILLIS } ?: false
        if (recentHardBrake) return false
        input.navigation.cautionAhead?.let { if (it.distanceMeters <= CAUTION_HOLD_METERS) return false }
        input.navigation.nextTurn?.takeIf { it.direction != TurnDirection.STRAIGHT }?.let { turn ->
            // 첫길 코치의 차선 준비 구간과 같다. 그 안에서는 회전 안내가 나오므로 메시지를 꺼내지 않는다.
            val holdMeters = if (turn.isHighwayJunction) CoachTimingUseCase.HIGHWAY_PREPARE_DISTANCE_M else CoachTimingUseCase.PREPARE_DISTANCE_M
            if (turn.distanceMeters <= holdMeters) return false
            if (turn.distanceMeters / (speed / KMH_PER_MS) <= CoachTimingUseCase.PREPARE_SECONDS) return false
        }
        return true
    }

    /** 조용한 순간이 [SETTLE_MILLIS] 이상 이어졌는지. 정차 중이면 바로 true. */
    fun isGoodMoment(quietSinceMillis: Long?, nowMillis: Long, isStopped: Boolean): Boolean {
        val since = quietSinceMillis ?: return false
        return isStopped || nowMillis - since >= SETTLE_MILLIS
    }

    companion object {
        const val STOPPED_KMH = 3f
        const val HOLD_TENSION = 30
        const val SETTLE_MILLIS = 5_000L
        /** 같은 운전 중 묻는 간격. 새 메시지가 와도 이보다 자주 묻지 않는다. */
        const val ASK_GAP_MILLIS = 60_000L
        private const val RECENT_HARD_BRAKE_MILLIS = 10_000L
        private const val CAUTION_HOLD_METERS = 500
        private const val ARRIVAL_HOLD_METERS = 500
        private const val KMH_PER_MS = 3.6f
    }
}
