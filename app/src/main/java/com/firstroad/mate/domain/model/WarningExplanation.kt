package com.firstroad.mate.domain.model

/**
 * 경고 하나에 대한 해설.
 *
 * @param suggestedStop 들르자고 제안할 곳. 바로 정차 등급은 '가장 가까운 쉴 곳'이지 정비소까지 운전하는 것이 아니다.
 */
data class WarningExplanation(
    val warning: VehicleWarning,
    val level: WarningLevel,
    val suggestedStop: PlaceCategory?,
)
