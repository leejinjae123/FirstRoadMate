package com.firstroad.mate.data.datasource.voice.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.firstroad.mate.data.datasource.voice.SpeechInputDataSource
import com.firstroad.mate.domain.model.RecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Pleos STT를 쓸 수 없는 환경에서 쓰는 Android 음성 인식. 인식기가 없으면 Unavailable. */
@Singleton
class AndroidSpeechInputDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechInputDataSource {

    override suspend fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override suspend fun recognize(): RecognitionResult {
        if (!isAvailable()) return RecognitionResult.Unavailable
        return withContext(Dispatchers.Main) {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            try {
                withTimeoutOrNull(LISTEN_TIMEOUT_MILLIS) { listenOnce(recognizer) } ?: RecognitionResult.NoMatch
            } finally {
                recognizer.destroy()
            }
        }
    }

    private suspend fun listenOnce(recognizer: SpeechRecognizer): RecognitionResult =
        suspendCancellableCoroutine { cont ->
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (cont.isActive) {
                        cont.resume(if (text.isNullOrBlank()) RecognitionResult.NoMatch else RecognitionResult.Text(text))
                    }
                }

                override fun onError(error: Int) {
                    if (cont.isActive) cont.resume(RecognitionResult.NoMatch)
                }

                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            cont.invokeOnCancellation { recognizer.cancel() }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                // 개발·에뮬레이터용 대체 경로: 가능하면 기기 안에서 인식한다 (실차는 Pleos ON_DEVICE만 사용).
                .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            recognizer.startListening(intent)
        }

    private companion object {
        const val LISTEN_TIMEOUT_MILLIS = 7_000L
    }
}
