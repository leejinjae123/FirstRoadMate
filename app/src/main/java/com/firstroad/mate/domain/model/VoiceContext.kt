package com.firstroad.mate.domain.model

/** 음성 명령을 실행할 때 필요한 지금의 상황. */
data class VoiceContext(
    val navigation: NavigationState,
    val vehicle: VehicleSnapshot,
    val location: GeoPoint?,
    val destination: GeoPoint?,
    val lastGuidance: Guidance?,
    val nowMillis: Long,
    val screen: VoiceScreen = VoiceScreen.DRIVE,
)
