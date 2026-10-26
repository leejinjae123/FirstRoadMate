package com.firstroad.mate.domain.model

/** 확인 질문에 "응"이라고 답했을 때 실행할 일. */
sealed interface PendingAction {
    data class AddWaypoint(val category: PlaceCategory) : PendingAction

    /** P단·정차 상태에서만 묻는다 (주행 중 창문 제어 안 함). */
    data class CloseWindow(val area: CabinArea) : PendingAction

    /** 긴장 구간 공조 자동 조절 사전 동의. 정차(하차 체크) 중에 한 번만 묻고, 대답(응/아니)을 저장한다. */
    data object AllowCabinAdjust : PendingAction
}
