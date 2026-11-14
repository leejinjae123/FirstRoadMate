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
    /** 메시지를 꺼내도 되는 조용한 구간이 시작된 시각. 지금 조용하지 않으면 null. */
    val quietSinceMillis: Long? = null,
    /** 이미 "읽어 드릴까요?"라고 물은 메시지 (같은 메시지로 다시 묻지 않는다). */
    val announcedMessageIds: Set<String> = emptySet(),
    val lastMessageAskAtMillis: Long? = null,
    /** 운전자가 "메시지 읽어 줘"라고 했는데 회전·긴장 구간이라 미뤄 둔 상태. */
    val isMessageReadDeferred: Boolean = false,
)
