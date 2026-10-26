package com.firstroad.mate.data.datasource.voice.android

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.firstroad.mate.data.datasource.voice.SpeechOutputDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Pleos TTS를 쓸 수 없는 환경(일반 에뮬레이터)에서 쓰는 Android 기본 TTS. */
@Singleton
class AndroidSpeechOutputDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechOutputDataSource {

    private val mutex = Mutex()
    private var tts: TextToSpeech? = null
    private var failed = false
    private val pending = mutableMapOf<String, CancellableContinuation<Unit>>()

    override suspend fun isAvailable(): Boolean = engineOrNull() != null

    override suspend fun speak(text: String) {
        val engine = engineOrNull() ?: return
        val utteranceId = UUID.randomUUID().toString()
        withTimeoutOrNull(MAX_SPEECH_BASE_MILLIS + text.length * MILLIS_PER_CHAR) {
            suspendCancellableCoroutine { cont ->
                synchronized(pending) { pending[utteranceId] = cont }
                cont.invokeOnCancellation {
                    synchronized(pending) { pending.remove(utteranceId) }
                    engine.stop()
                }
                val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (result != TextToSpeech.SUCCESS) finish(utteranceId)
            }
        }
    }

    private fun finish(utteranceId: String?) {
        val cont = synchronized(pending) { pending.remove(utteranceId) } ?: return
        if (cont.isActive) cont.resume(Unit)
    }

    private suspend fun engineOrNull(): TextToSpeech? = mutex.withLock {
        if (tts != null || failed) return@withLock tts
        var engine: TextToSpeech? = null
        val ready = withTimeoutOrNull(INIT_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { cont ->
                engine = TextToSpeech(context) { status ->
                    if (cont.isActive) cont.resume(status == TextToSpeech.SUCCESS)
                }
            }
        } == true
        val created = engine
        if (ready && created != null) {
            created.language = Locale.KOREAN
            created.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) = finish(utteranceId)

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) = finish(utteranceId)

                override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId)
            })
            tts = created
        } else {
            created?.shutdown()
            failed = true
        }
        tts
    }

    private companion object {
        const val INIT_TIMEOUT_MILLIS = 3_000L
        const val MAX_SPEECH_BASE_MILLIS = 3_000L
        const val MILLIS_PER_CHAR = 250L
    }
}
