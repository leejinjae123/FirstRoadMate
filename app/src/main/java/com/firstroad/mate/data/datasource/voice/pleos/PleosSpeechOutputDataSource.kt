package com.firstroad.mate.data.datasource.voice.pleos

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import com.firstroad.mate.data.datasource.SDK_CALLBACK_TIMEOUT_MILLIS
import com.firstroad.mate.data.datasource.awaitSdkCallback
import com.firstroad.mate.data.datasource.voice.SpeechOutputDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Pleos TextToSpeech (ON_DEVICE: 앱 등록·네트워크 불필요).
 * SDK 이벤트에 발화 ID가 없어서, 우리가 stop()으로 끊은 횟수만큼 뒤늦게 오는 onStop을 무시한다.
 * 그래야 이전 발화의 onStop이 새 발화를 끝내 버리지 않는다.
 */
@Singleton
class PleosSpeechOutputDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechOutputDataSource {

    private val initMutex = Mutex()
    private val lock = Any()
    private var tts: TextToSpeech? = null
    private var failed = false
    private var pending: CancellableContinuation<Unit>? = null
    private var expectedStopEvents = 0

    private val eventListener = object : EventListener {
        override fun onReady() = Unit
        override fun onStart() = Unit
        override fun onDone() = finishPending()
        override fun onStop() = onStopEvent()
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onError(errorType: ErrorType) = finishPending()
    }

    override suspend fun isAvailable(): Boolean = engineOrNull() != null

    override suspend fun speak(text: String) {
        val engine = engineOrNull() ?: return
        // 시간 초과나 호출 측 취소 모두 invokeOnCancellation에서 그 발화만 멈춘다.
        withTimeoutOrNull(maxSpeechMillis(text)) {
            suspendCancellableCoroutine { cont ->
                synchronized(lock) { pending = cont }
                cont.invokeOnCancellation { interrupt(engine, cont) }
                runCatching { engine.speak(text) }.onFailure { finishPending() }
            }
        }
    }

    private fun interrupt(engine: TextToSpeech, cont: CancellableContinuation<Unit>) {
        synchronized(lock) {
            if (pending === cont) pending = null
            expectedStopEvents++
        }
        runCatching { engine.stop() }
    }

    private fun onStopEvent() {
        synchronized(lock) {
            if (expectedStopEvents > 0) {
                expectedStopEvents--
                return
            }
        }
        finishPending()
    }

    private fun finishPending() {
        val cont = synchronized(lock) { pending.also { pending = null } } ?: return
        if (cont.isActive) cont.resume(Unit)
    }

    private suspend fun engineOrNull(): TextToSpeech? = initMutex.withLock {
        if (tts != null || failed) return@withLock tts
        val candidate = runCatching { TextToSpeech(context, Mode.ON_DEVICE) }.getOrNull()
        val ready = candidate != null && awaitSdkCallback<Boolean> { resume ->
            candidate.initialize(object : OnInitializeListener {
                override fun onSuccess() = resume(true)
                override fun onFailure(errorType: ErrorType) = resume(false)
            })
        } == true
        if (ready) {
            runCatching {
                candidate.setLocale(Locale.KOREA)
                candidate.addEventListener(eventListener)
            }
            tts = candidate
        } else {
            failed = true
        }
        tts
    }

    /** 한국어 안내 기준 넉넉한 최대 발화 시간 (끝 이벤트가 오지 않아도 멈추지 않게). */
    private fun maxSpeechMillis(text: String): Long = SDK_CALLBACK_TIMEOUT_MILLIS + text.length * MILLIS_PER_CHAR

    private companion object {
        const val MILLIS_PER_CHAR = 250L
    }
}
