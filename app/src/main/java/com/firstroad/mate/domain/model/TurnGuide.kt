package com.firstroad.mate.domain.model

/**
 * 다음 회전 지점 안내.
 *
 * @param id 같은 회전 지점을 구분하는 키. 안내 중복을 막는 데 쓴다.
 * @param isHighwayJunction 고속도로 합류·분기 지점이면 true.
 */
data class TurnGuide(
    val id: String,
    val direction: TurnDirection,
    val distanceMeters: Int,
    val roadName: String?,
    val isHighwayJunction: Boolean,
)
