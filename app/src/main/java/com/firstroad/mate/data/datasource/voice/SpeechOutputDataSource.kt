package com.firstroad.mate.data.datasource.voice

interface SpeechOutputDataSource {
    /** 이 환경에서 쓸 수 있는지. */
    suspend fun isAvailable(): Boolean

    /** 문장을 말하고, 끝나면 돌아온다. 코루틴이 취소되면 그 문장만 멈춘다. */
    suspend fun speak(text: String)
}
