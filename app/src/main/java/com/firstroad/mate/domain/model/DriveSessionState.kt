package com.firstroad.mate.domain.model

/** 한 번의 주행 동안 판단에 필요한 기억. DriveTickUseCase가 매 틱 새 상태를 돌려준다. */
data class DriveSessionState(
    val startedAtMillis: Long? = null,
    val isBriefed: Boolean = false,
    val handledKeys: Set<String> = emptySet(),
    val seenWarnings: Set<WarningType> = emptySet(),
    val stats: DriveStats = DriveStats(),
    val lastSpeedKmh: Float? = null,
    val lastSpeedAtMillis: Long = 0,
    val lastHardBrakeAtMillis: Long? = null,
    val isCalmActive: Boolean = false,
    val isArrived: Boolean = false,
    val isFinished: Boolean = false,
    val tension: Int = 0,
)
