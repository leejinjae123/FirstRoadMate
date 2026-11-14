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
    /** 아직 읽어 주지 않은 메신저 메시지. */
    val messages: List<IncomingMessage> = emptyList(),
    /** 화면에 대답을 기다리는 확인 질문이 있으면 true (질문을 겹쳐 묻지 않는다). */
    val hasOpenQuestion: Boolean = false,
)
