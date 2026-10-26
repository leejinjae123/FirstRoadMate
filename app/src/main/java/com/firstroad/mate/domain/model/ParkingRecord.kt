package com.firstroad.mate.domain.model

data class ParkingRecord(
    val location: GeoPoint,
    val placeName: String,
    val memo: String?,
    val savedAtMillis: Long,
)
