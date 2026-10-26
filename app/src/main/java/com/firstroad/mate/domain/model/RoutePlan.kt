package com.firstroad.mate.domain.model

/**
 * 초보 맞춤 경로 계획.
 *
 * @param waypoints 최대 2개 (NaviHelper 제약: WaypointIndex.FIRST / SECOND).
 */
data class RoutePlan(
    val destination: Place,
    val waypoints: List<WaypointSuggestion>,
    val preference: RoutePreference,
    val estimatedMinutes: Int,
) {
    init {
        require(waypoints.size <= MAX_WAYPOINTS) { "경유지는 최대 $MAX_WAYPOINTS 개입니다." }
    }

    companion object {
        const val MAX_WAYPOINTS = 2
    }
}
