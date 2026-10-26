package com.firstroad.mate.domain.model

/** 차량 제어(창문 닫기, 공조 조절 등) 요청 결과. */
enum class ControlResult {
    DONE,

    /** 권한이 없거나 App Market에서 승인되지 않음 → 안내 모드로 대체. */
    NOT_PERMITTED,

    /** 차종이 지원하지 않음 (capability 없음). */
    NOT_SUPPORTED,
    FAILED,
}
