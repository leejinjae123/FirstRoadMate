package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.RecognitionResult

interface VoiceRepository {
    /**
     * 문장을 차량 스피커로 말하고, 끝나면 돌아온다. 코루틴이 취소되면 그 문장만 멈추므로,
     * 우선순위가 높은 안내가 오면 호출한 쪽이 이전 말하기 작업을 취소한다 (음성 에티켓).
     */
    suspend fun speak(text: String)

    /** 음성 인식을 한 번 수행한다 (확인 질문 대답, 음성 명령). */
    suspend fun recognize(): RecognitionResult
}
