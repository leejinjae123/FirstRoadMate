package com.firstroad.mate.data.datasource.navi.pleos

import ai.pleos.playground.navi.helper.NaviHelper
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** NaviHelper 연결을 앱 전체에서 하나로 관리한다. 연결되지 않으면 null. */
@Singleton
class PleosNaviClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    private var helper: NaviHelper? = null
    private var failed = false

    suspend fun helperOrNull(): NaviHelper? = mutex.withLock {
        if (helper != null || failed) return@withLock helper
        val candidate = runCatching { NaviHelper(context) }.getOrNull() ?: return@withLock fail(null)
        val connected = withTimeoutOrNull(CONNECT_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    candidate.initialize(
                        onConnected = { if (cont.isActive) cont.resume(true) },
                        onFailed = { error ->
                            Log.w(TAG, "NaviHelper 연결 실패", error)
                            if (cont.isActive) cont.resume(false)
                        },
                    )
                }.onFailure { if (cont.isActive) cont.resume(false) }
            }
        } ?: false
        if (connected) candidate.also { helper = it } else fail(candidate)
    }

    private fun fail(candidate: NaviHelper?): NaviHelper? {
        runCatching { candidate?.release() }
        failed = true
        return null
    }

    private companion object {
        const val TAG = "PleosNaviClient"
        const val CONNECT_TIMEOUT_MILLIS = 3_000L
    }
}
