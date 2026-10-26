package com.firstroad.mate.domain.model

/** 내비게이션 이벤트들을 한 시점의 상태로 모은 것. */
data class NavigationState(
    val isRouteActive: Boolean = false,
    val nextTurn: TurnGuide? = null,
    val upcomingTurns: List<TurnGuide> = emptyList(),
    val cautionAhead: CautionPoint? = null,
    val speedLimitKmh: Int? = null,
    val remainingMeters: Int? = null,
    val remainingSeconds: Int? = null,
    val waypointCount: Int = 0,
    val isArrived: Boolean = false,
)
