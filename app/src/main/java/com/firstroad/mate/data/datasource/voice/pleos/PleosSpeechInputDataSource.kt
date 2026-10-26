package com.firstroad.mate.data.datasource.voice.pleos

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import com.firstroad.mate.data.datasource.awaitSdkCallback
import com.firstroad.mate.data.datasource.voice.SpeechInputDataSource
import com.firstroad.mate.domain.model.RecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Pleos SpeechToText (ON_DEVICE). 녹음 원본은 저장하지 않고 인식된 텍스트만 돌려준다. */
@Singleton
class PleosSpeechInputDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechInputDataSource {

    private val mutex = Mutex()
    private var stt: SpeechToText? = null
    private var failed = false

    override suspend fun isAvailable(): Boolean = engineOrNull() != null

    override suspend fun recognize(): RecognitionResult {
        val engine = engineOrNull() ?: return RecognitionResult.Unavailable
        var listener: ResultListener? = null
        try {
            return awaitSdkCallback<RecognitionResult>(LISTEN_TIMEOUT_MILLIS) { resume ->
                val created = resultListener(resume)
                listener = created
                engine.addListener(created)
                engine.request()
            } ?: RecognitionResult.NoMatch
        } finally {
            runCatching { engine.stop() }
            listener?.let { runCatching { engine.removeListener(it) } }
        }
    }

    private fun resultListener(resume: (RecognitionResult?) -> Unit) = object : ResultListener {
        override fun onReady() = Unit
        override fun onStartedRecognition() = Unit
        override fun onUpdatedEpdData(on: Long, off: Long) = Unit
        override fun onUpdatedRms(rms: Double) = Unit

        override fun onUpdated(stt: String, completed: Boolean) {
            if (completed) resume(if (stt.isBlank()) RecognitionResult.NoMatch else RecognitionResult.Text(stt))
        }

        override fun onEndedRecognition() = resume(RecognitionResult.NoMatch)
        override fun onError(errorType: ErrorType) = resume(RecognitionResult.NoMatch)
    }

    private suspend fun engineOrNull(): SpeechToText? = mutex.withLock {
        if (stt != null || failed) return@withLock stt
        val candidate = runCatching { SpeechToText(context, Mode.ON_DEVICE) }.getOrNull()
        val ready = candidate != null && awaitSdkCallback<Boolean> { resume ->
            candidate.initialize(object : OnInitializeListener {
                override fun onSuccess() = resume(true)
                override fun onFailure(errorType: ErrorType) = resume(false)
            })
        } == true
        if (ready) {
            runCatching { candidate.setLocale(Locale.KOREA) }
            stt = candidate
        } else {
            failed = true
        }
        stt
    }

    private companion object {
        /** 확인 질문 뒤 대답을 기다리는 시간 (기획서 4.4: 7초). */
        const val LISTEN_TIMEOUT_MILLIS = 7_000L
    }
}
