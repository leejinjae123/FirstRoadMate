package com.firstroad.mate.domain.model

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val location: GeoPoint,
    val category: PlaceCategory,
    val distanceMeters: Int? = null,
)
