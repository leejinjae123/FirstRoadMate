package com.firstroad.mate.data.datasource.message

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 기기에 뜨는 메신저 알림(카카오톡·문자 등)을 받아 보관함에 넣는다.
 * 사용자가 설정의 '알림 접근'에서 허용해야 동작한다. 휴대폰 연동·답장은 하지 않는다.
 */
@AndroidEntryPoint
class MessageNotificationService : NotificationListenerService() {

    @Inject lateinit var inbox: MessageInboxDataSource

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val message = sbn?.let { MessageNotificationParser.parse(it, packageName) } ?: return
        inbox.add(message)
    }
}
