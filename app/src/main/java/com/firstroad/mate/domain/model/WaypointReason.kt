package com.firstroad.mate.domain.model

/** 경유지를 추천한 이유. 화면과 음성에서 "왜?"를 설명하는 데 쓴다. */
enum class WaypointReason {
    LOW_FUEL,
    LOW_BATTERY,
    EASY_PARKING,
    LONG_DRIVE_REST,
    REPAIR_NEEDED,
    REQUESTED_BY_VOICE,
}
