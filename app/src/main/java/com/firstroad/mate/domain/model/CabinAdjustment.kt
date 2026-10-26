package com.firstroad.mate.domain.model

/** 카밍 캐빈이 적용할 실내 조절 값. 허용 범위는 기획서 3장(온도 −1℃, 풍량 +1단)을 따른다. */
data class CabinAdjustment(
    val temperatureDeltaCelsius: Int,
    val fanSpeedDelta: Int,
    val calmAmbientLight: Boolean,
)
