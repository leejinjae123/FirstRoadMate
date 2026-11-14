package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.IncomingMessage
import kotlinx.coroutines.flow.Flow

/** 메신저 메시지 읽어 주기. 기기 안의 알림만 읽는다 (휴대폰 연동·답장 없음). */
interface MessageRepository {
    /** 아직 읽어 주지 않은 메시지 (오래된 것부터). */
    fun unreadMessages(): Flow<List<IncomingMessage>>

    /** 알림 읽기를 쓸 수 있는지 (데모는 항상 true, 실차는 '알림 접근'을 허용했을 때). */
    suspend fun isReadoutAvailable(): Boolean

    /** 읽어 준 메시지를 목록에서 뺀다. */
    suspend fun markRead(ids: Collection<String>)
}
