package com.firstroad.mate.data.datasource.voice

import com.firstroad.mate.domain.model.RecognitionResult

interface SpeechInputDataSource {
    suspend fun isAvailable(): Boolean

    /** 한 번 듣고 결과를 돌려준다. 시간 안에 결과가 없으면 NoMatch. */
    suspend fun recognize(): RecognitionResult
}
