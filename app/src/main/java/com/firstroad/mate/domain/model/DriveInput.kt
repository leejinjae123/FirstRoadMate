package com.firstroad.mate.domain.model

/** 한 틱의 입력: 차량·내비 상태와 주변 정보. */
data class DriveInput(
    val vehicle: VehicleSnapshot,
    val navigation: NavigationState,
    val level: BeginnerLevel,
    val nowMillis: Long,
    val location: GeoPoint?,
    val destination: GeoPoint?,
    val difficultSpots: List<DifficultSpot>,
)
