package com.firstroad.mate.data.repository

import com.firstroad.mate.data.datasource.PlatformSource
import com.firstroad.mate.data.datasource.PleosSource
import com.firstroad.mate.data.datasource.voice.SpeechInputDataSource
import com.firstroad.mate.data.datasource.voice.SpeechOutputDataSource
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.repository.VoiceRepository
import javax.inject.Inject

/** Pleos 음성 SDK를 먼저 쓰고, 쓸 수 없는 환경이면 Android 기본 음성 엔진으로 대체한다. */
class VoiceRepositoryImpl @Inject constructor(
    @PleosSource private val pleosOutput: SpeechOutputDataSource,
    @PlatformSource private val platformOutput: SpeechOutputDataSource,
    @PleosSource private val pleosInput: SpeechInputDataSource,
    @PlatformSource private val platformInput: SpeechInputDataSource,
) : VoiceRepository {

    override suspend fun speak(text: String) {
        val output = if (pleosOutput.isAvailable()) pleosOutput else platformOutput
        output.speak(text)
    }

    override suspend fun recognize(): RecognitionResult {
        val input = if (pleosInput.isAvailable()) pleosInput else platformInput
        return input.recognize()
    }
}
