package com.firstroad.mate.ui.common

/** 안내를 어떻게 말할지. */
enum class SpeechDelivery {
    /** 음성 에티켓(간격·회전 직전 침묵·우선순위)을 따른다. 대답 이상의 중요한 안내는 버리지 않고 대기열에 넣는다. */
    NORMAL,

    /** 지금 말하는 것이 끝난 뒤에 이어서 말한다 (확인 질문). */
    QUEUE,

    /** 운전자가 직접 요청한 것("다시 듣기")이라 바로 말한다. */
    FORCE,
}
