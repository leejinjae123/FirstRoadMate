package com.firstroad.mate.data.datasource

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** SDK 일회성 콜백의 기본 대기 시간. 콜백이 오지 않아도 코루틴이 영원히 멈추지 않게 한다. */
internal const val SDK_CALLBACK_TIMEOUT_MILLIS = 3_000L

/**
 * 콜백 하나로 결과를 주는 SDK 호출을 suspend로 바꾼다.
 * 시간 안에 결과가 없거나 SDK가 예외를 던지면 null. 여러 번 호출된 콜백은 첫 번째만 쓴다.
 */
internal suspend fun <T> awaitSdkCallback(
    timeoutMillis: Long = SDK_CALLBACK_TIMEOUT_MILLIS,
    call: (resume: (T?) -> Unit) -> Unit,
): T? = withTimeoutOrNull(timeoutMillis) {
    suspendCancellableCoroutine { cont ->
        val resume: (T?) -> Unit = { value -> if (cont.isActive) cont.resume(value) }
        runCatching { call(resume) }.onFailure { resume(null) }
    }
}
