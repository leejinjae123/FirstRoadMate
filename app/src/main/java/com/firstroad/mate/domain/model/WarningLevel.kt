package com.firstroad.mate.domain.model

/** 경고등 통역사의 3단계 위험 등급. */
enum class WarningLevel {
    /** 안전한 곳에 바로 정차해야 함. */
    STOP_NOW,

    /** 계속 가도 되지만 오늘 중 점검이 필요함. */
    CHECK_TODAY,

    /** 참고만 하면 됨. */
    INFO,
}
