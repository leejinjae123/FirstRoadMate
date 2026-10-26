package com.firstroad.mate.domain.model

/** 확인 질문에 "응"이라고 답했을 때 실행할 일. */
sealed interface PendingAction {
    data class AddWaypoint(val category: PlaceCategory) : PendingAction

    /** P단·정차 상태에서만 묻는다 (주행 중 창문 제어 안 함). */
    data class CloseWindow(val area: CabinArea) : PendingAction
}
