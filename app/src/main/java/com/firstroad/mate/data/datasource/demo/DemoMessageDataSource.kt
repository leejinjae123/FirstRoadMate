package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.domain.model.IncomingMessage
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

/** 데모 출근길에 정해진 시각에 도착하는 메시지 (실차의 알림 서비스 대신). */
@Singleton
class DemoMessageDataSource @Inject constructor(
    private val tripState: DemoTripState,
) {
    fun unread(): Flow<List<IncomingMessage>> = flow {
        while (currentCoroutineContext().isActive) {
            val state = tripState.state.value
            val elapsed = tripState.elapsedSeconds(System.currentTimeMillis())
            val arrived = state.startedAtMillis?.let { DemoTripScript.messagesAt(elapsed, it) }.orEmpty()
            emit(arrived.filterNot { it.id in state.readMessageIds })
            delay(TICK_MILLIS)
        }
    }.distinctUntilChanged()

    fun markRead(ids: Collection<String>) = tripState.markMessagesRead(ids)

    private companion object {
        const val TICK_MILLIS = 500L
    }
}
