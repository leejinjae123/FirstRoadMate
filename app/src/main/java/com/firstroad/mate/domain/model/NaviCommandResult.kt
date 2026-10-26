package com.firstroad.mate.domain.model

/** 내비 요청(경로 생성, 경유지 추가·삭제) 결과. 실제 경로 완성은 NavigationState로 확인한다. */
enum class NaviCommandResult {
    REQUESTED,
    WAYPOINT_FULL,
    NOT_AVAILABLE,
    FAILED,
}
