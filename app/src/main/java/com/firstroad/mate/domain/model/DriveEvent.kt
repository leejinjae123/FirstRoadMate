package com.firstroad.mate.domain.model

/** 주행 판단이 만들어 낸, 화면(ViewModel)이 실행할 일. */
sealed interface DriveEvent {
    data class Say(val guidance: Guidance) : DriveEvent

    /** 확인 질문: 말하고, "응"이면 action을 실행한다. */
    data class Ask(val guidance: Guidance, val action: PendingAction) : DriveEvent

    data class ShowWarning(val explanation: WarningExplanation) : DriveEvent

    /** 카밍 캐빈 시작: 실내 조절을 요청하고 결과를 말한다. */
    data object StartCalm : DriveEvent

    /** 긴장 구간을 지남: 실내 조절을 원래대로 되돌린다. */
    data object EndCalm : DriveEvent

    /** 도착 후 P단: 주행 기록을 저장하고 하차 체크로 넘어간다. */
    data object Finished : DriveEvent
}
