package com.firstroad.mate.domain.model

/** 운전자 발화를 해석한 결과 (기획서 4.2 음성 명령 카탈로그). */
sealed interface VoiceCommand {
    data class AddWaypoint(val category: PlaceCategory) : VoiceCommand
    data object RemoveWaypoint : VoiceCommand
    data object NextTurn : VoiceCommand
    data object Repeat : VoiceCommand
    data object ExplainWarning : VoiceCommand
    data object CalmDown : VoiceCommand
    data object MarkDifficultSpot : VoiceCommand
    data object WhereIsMyCar : VoiceCommand
    data class ParkingMemo(val memo: String) : VoiceCommand
    data object Yes : VoiceCommand
    data object No : VoiceCommand
    data class Unknown(val text: String) : VoiceCommand
}
