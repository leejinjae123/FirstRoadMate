package com.firstroad.mate.data.datasource.message

import com.firstroad.mate.domain.model.IncomingMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림에서 읽은 메시지를 잠깐 들고 있는 메모리 보관함. 디스크에 저장하지 않는다 (개인 메시지라서).
 * 알림 서비스가 넣고, 주행 화면이 읽어 준 뒤 뺀다. 오래된 메시지는 읽어 주지 않는다.
 */
@Singleton
class MessageInboxDataSource @Inject constructor() {

    private val messages = MutableStateFlow<List<IncomingMessage>>(emptyList())

    fun unread(): Flow<List<IncomingMessage>> =
        messages.map { list -> list.filter { System.currentTimeMillis() - it.receivedAtMillis < KEEP_MILLIS } }

    /** 같은 id(같은 알림의 같은 내용)는 한 번만 넣는다. */
    fun add(message: IncomingMessage) = messages.update { list ->
        if (list.any { it.id == message.id }) {
            list
        } else {
            (list + message).filter { message.receivedAtMillis - it.receivedAtMillis < KEEP_MILLIS }.takeLast(MAX_MESSAGES)
        }
    }

    fun markRead(ids: Collection<String>) = messages.update { list -> list.filterNot { it.id in ids } }

    private companion object {
        const val KEEP_MILLIS = 30 * 60_000L
        const val MAX_MESSAGES = 20
    }
}
