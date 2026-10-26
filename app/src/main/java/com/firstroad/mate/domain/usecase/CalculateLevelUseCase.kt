package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.BeginnerLevel
import com.firstroad.mate.domain.model.DriveRecord
import com.firstroad.mate.domain.model.LevelTier
import javax.inject.Inject

/**
 * 주행 기록으로 초보 레벨을 계산한다 (기획서 6.4).
 * - 3단계 이상: 깜빡이 준수율 80% 이상
 * - 4단계 이상: 주행당 급제동 평균 1회 이하
 */
class CalculateLevelUseCase @Inject constructor() {

    operator fun invoke(records: List<DriveRecord>): BeginnerLevel {
        val drives = records.size
        val signalUsed = records.sumOf { it.stats.signalUsed }
        val signalTotal = records.sumOf { it.stats.signalTotal }
        val signalRate = if (signalTotal == 0) 1f else signalUsed.toFloat() / signalTotal
        val hardBrakeAverage = if (drives == 0) 0f else records.sumOf { it.stats.hardBrakes }.toFloat() / drives

        val tier = LevelTier.entries.last { tier ->
            drives >= tier.requiredDrives && when {
                tier.level >= LevelTier.ALMOST.level ->
                    signalRate >= MIN_SIGNAL_RATE && hardBrakeAverage <= MAX_HARD_BRAKE_AVERAGE
                tier.level >= LevelTier.ADAPTING.level -> signalRate >= MIN_SIGNAL_RATE
                else -> true
            }
        }
        val next = LevelTier.entries.getOrNull(tier.ordinal + 1)
        val progress = if (next == null) {
            1f
        } else {
            val span = (next.requiredDrives - tier.requiredDrives).coerceAtLeast(1)
            ((drives - tier.requiredDrives).toFloat() / span).coerceIn(0f, 1f)
        }
        return BeginnerLevel(tier, drives, progress)
    }

    private companion object {
        const val MIN_SIGNAL_RATE = 0.8f
        const val MAX_HARD_BRAKE_AVERAGE = 1f
    }
}
