package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CautionType
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.VehicleSnapshot
import javax.inject.Inject

/**
 * ⑤ 카밍 캐빈: 지금부터 짧은 시간 동안 운전자가 얼마나 긴장할지(0~100)를 추정한다.
 * 가중치는 초기값이며 사용자 테스트로 조정한다.
 */
class TensionScoreUseCase @Inject constructor() {

    operator fun invoke(
        navigation: NavigationState,
        vehicle: VehicleSnapshot,
        recentHardBrake: Boolean,
        nearDifficultSpot: Boolean,
    ): Int {
        var score = 0
        navigation.cautionAhead?.let { caution ->
            if (caution.distanceMeters <= CAUTION_RANGE_M) {
                score += if (caution.type in HIGH_STRESS_CAUTIONS) 35 else 20
            }
        }
        navigation.nextTurn?.let { turn ->
            if (turn.isHighwayJunction && turn.distanceMeters <= JUNCTION_RANGE_M) score += 30
            else if (turn.distanceMeters <= TURN_RANGE_M) score += 15
        }
        if (vehicle.isWiperOn == true) score += 15
        if (vehicle.isHeadlightOn == true) score += 10
        if (recentHardBrake) score += 20
        if (nearDifficultSpot) score += 20
        return score.coerceIn(0, 100)
    }

    /** 긴장 지수가 높으면 실내를 차분하게 만드는 조절값. 기준 미만이면 null. */
    fun adjustmentFor(score: Int): CabinAdjustment? =
        if (score >= CALM_THRESHOLD) CALM_ADJUSTMENT else null

    companion object {
        const val CALM_THRESHOLD = 60
        val CALM_ADJUSTMENT = CabinAdjustment(temperatureDeltaCelsius = -1, fanSpeedDelta = 1, calmAmbientLight = true)
        private const val CAUTION_RANGE_M = 500
        private const val JUNCTION_RANGE_M = 800
        private const val TURN_RANGE_M = 200
        private val HIGH_STRESS_CAUTIONS = setOf(CautionType.MERGE, CautionType.SHARP_CURVE, CautionType.ACCIDENT_ZONE)
    }
}
