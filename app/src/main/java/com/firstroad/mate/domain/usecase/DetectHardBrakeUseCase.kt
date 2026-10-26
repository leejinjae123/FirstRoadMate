package com.firstroad.mate.domain.usecase

import javax.inject.Inject

/** 두 시점의 속도로 급제동(감속도 3.5 m/s² 이상)을 판정한다. */
class DetectHardBrakeUseCase @Inject constructor() {

    operator fun invoke(
        previousSpeedKmh: Float?,
        previousAtMillis: Long,
        currentSpeedKmh: Float?,
        currentAtMillis: Long,
    ): Boolean {
        if (previousSpeedKmh == null || currentSpeedKmh == null) return false
        val seconds = (currentAtMillis - previousAtMillis) / 1000f
        if (seconds <= 0f || seconds > MAX_WINDOW_SECONDS) return false
        val decelerationMs2 = (previousSpeedKmh - currentSpeedKmh) / 3.6f / seconds
        return decelerationMs2 >= HARD_BRAKE_MS2
    }

    private companion object {
        const val HARD_BRAKE_MS2 = 3.5f
        const val MAX_WINDOW_SECONDS = 2f
    }
}
