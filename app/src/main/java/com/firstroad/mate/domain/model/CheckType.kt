package com.firstroad.mate.domain.model

/**
 * 시동 끄기 전 점검 항목. 문 잠금은 차량의 오토 도어락·원격 제어와 겹치고,
 * P단에서 운전자가 아직 타고 있을 때 묻는 것은 소음이라 넣지 않는다.
 */
enum class CheckType {
    WINDOW,
    HEADLIGHT,
    PARKING_BRAKE,
}
