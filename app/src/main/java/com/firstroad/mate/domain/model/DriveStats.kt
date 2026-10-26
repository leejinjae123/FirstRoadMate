package com.firstroad.mate.domain.model

/** 한 번의 주행에서 모은 성장 리포트 지표. */
data class DriveStats(
    val signalUsed: Int = 0,
    val signalMissed: Int = 0,
    val hardBrakes: Int = 0,
    val difficultPassed: Int = 0,
    val distanceMeters: Int = 0,
    val durationSeconds: Int = 0,
) {
    val signalTotal: Int get() = signalUsed + signalMissed
}
