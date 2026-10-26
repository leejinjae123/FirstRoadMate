package com.firstroad.mate.domain.model

/** 운전자가 "여기 어려웠어"라고 표시한 지점. 다음 경로 브리핑에 반영한다. */
data class DifficultSpot(
    val id: Long = 0,
    val location: GeoPoint,
    val createdAtMillis: Long,
)
