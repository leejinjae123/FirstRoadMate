package com.firstroad.mate.ui.common

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 공통 ViewModel.
 * - 토스트: 유실돼도 무해한 알림만 Channel로 보낸다. 중요한 결과는 각 화면의 UiState로 다룬다.
 * - 중복 요청 방지: 같은 키의 작업이 실행 중이면 새 요청을 무시한다. 화면을 떠날 때는 [cancelRunning]으로 멈춘다.
 */
abstract class BaseViewModel : ViewModel() {

    private val toastChannel = Channel<Int>(Channel.BUFFERED)
    val toastEvents: Flow<Int> = toastChannel.receiveAsFlow()

    private val runningJobs = mutableMapOf<String, Job>()

    protected fun showToast(@StringRes messageId: Int) {
        toastChannel.trySend(messageId)
    }

    protected fun launchOnce(key: String, block: suspend CoroutineScope.() -> Unit) {
        if (runningJobs[key]?.isActive == true) return
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            try {
                block()
            } finally {
                if (runningJobs[key] == coroutineContext[Job]) runningJobs.remove(key)
            }
        }
        runningJobs[key] = job
        job.start()
    }

    protected fun cancelRunning(key: String) {
        runningJobs.remove(key)?.cancel()
    }
}
