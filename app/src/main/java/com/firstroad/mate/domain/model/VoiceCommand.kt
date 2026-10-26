package com.firstroad.mate.domain.model

/**
 * 운전자 발화를 해석한 결과 (기획서 4.2 음성 명령 카탈로그).
 * 화면의 모든 버튼은 같은 일을 하는 음성 명령이 있다 (출발·끝내기·나중에·창문 닫기 등).
 */
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

    /** 홈: 추천 경로로 출발. */
    data object Start : VoiceCommand

    /** 주행: 주행 끝내기 / 하차: 확인했어요(홈으로). */
    data object Finish : VoiceCommand

    /** 경고 카드 '나중에'. */
    data object Later : VoiceCommand

    /** 하차: 열린 창문 닫기. 주행 중에는 세운 뒤에 하도록 안내한다. */
    data object CloseWindow : VoiceCommand

    /** "뭐라고 말하면 돼?": 지금 화면에서 할 수 있는 말을 알려 준다. */
    data object Help : VoiceCommand
    data object Yes : VoiceCommand
    data object No : VoiceCommand
    data class Unknown(val text: String) : VoiceCommand
}
