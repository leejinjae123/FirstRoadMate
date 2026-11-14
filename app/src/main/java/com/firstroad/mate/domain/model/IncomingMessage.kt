package com.firstroad.mate.domain.model

/**
 * 기기에 온 메신저 메시지 한 건 (카카오톡·문자 등의 알림에서 읽은 것).
 * 내용은 메모리에만 두고 저장하지 않는다. 읽어 주거나 시간이 지나면 버린다.
 */
data class IncomingMessage(
    val id: String,
    /** 보낸 사람 (단체방이면 "보낸 사람" 그대로, 방 이름은 붙이지 않는다). */
    val sender: String,
    val text: String,
    val app: MessengerApp,
    val receivedAtMillis: Long,
)

/** 읽어 줄 때 "카카오톡", "문자"처럼 앱 이름을 붙이기 위한 구분. */
enum class MessengerApp {
    KAKAOTALK,
    SMS,
    OTHER,
}
