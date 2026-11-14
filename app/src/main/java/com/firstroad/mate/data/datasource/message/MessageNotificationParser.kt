package com.firstroad.mate.data.datasource.message

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.firstroad.mate.domain.model.IncomingMessage
import com.firstroad.mate.domain.model.MessengerApp

/**
 * 메신저 알림 → [IncomingMessage]. 메신저가 아닌 알림(광고·진행 중 알림·묶음 요약)은 null.
 * 카카오톡처럼 MessagingStyle을 쓰는 앱은 마지막 메시지의 보낸 사람·내용을, 아니면 제목·본문을 쓴다.
 */
object MessageNotificationParser {

    fun parse(sbn: StatusBarNotification, ownPackage: String): IncomingMessage? {
        if (sbn.packageName == ownPackage || sbn.isOngoing) return null
        val notification = sbn.notification ?: return null
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return null
        val app = appOf(sbn.packageName, notification.category) ?: return null

        val style = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        val last = style?.messages?.lastOrNull()
        val extras = notification.extras
        val sender = (last?.person?.name ?: extras.getCharSequence(Notification.EXTRA_TITLE))?.toString()?.trim()
        val text = (last?.text ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim()
        if (sender.isNullOrEmpty() || text.isNullOrEmpty()) return null

        val receivedAt = last?.timestamp?.takeIf { it > 0 } ?: sbn.postTime
        return IncomingMessage(
            id = "${sbn.key}|$receivedAt|${text.hashCode()}",
            sender = sender,
            text = text,
            app = app,
            receivedAtMillis = receivedAt,
        )
    }

    private fun appOf(packageName: String, category: String?): MessengerApp? = when {
        packageName == KAKAOTALK -> MessengerApp.KAKAOTALK
        packageName in SMS_PACKAGES -> MessengerApp.SMS
        category == Notification.CATEGORY_MESSAGE -> MessengerApp.OTHER
        else -> null
    }

    private const val KAKAOTALK = "com.kakao.talk"
    private val SMS_PACKAGES = setOf(
        "com.samsung.android.messaging",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.android.messaging",
    )
}
