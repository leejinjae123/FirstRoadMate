package com.firstroad.mate.data.repository

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.firstroad.mate.data.datasource.demo.DemoMessageDataSource
import com.firstroad.mate.data.datasource.message.MessageInboxDataSource
import com.firstroad.mate.domain.model.IncomingMessage
import com.firstroad.mate.domain.repository.MessageRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/** 실차는 알림 서비스가 모은 메시지, 데모는 시나리오 메시지를 쓴다. */
@OptIn(ExperimentalCoroutinesApi::class)
class MessageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inbox: MessageInboxDataSource,
    private val demo: DemoMessageDataSource,
    private val settingsRepository: SettingsRepository,
) : MessageRepository {

    override fun unreadMessages(): Flow<List<IncomingMessage>> =
        settingsRepository.isDemoActive().flatMapLatest { isDemo -> if (isDemo) demo.unread() else inbox.unread() }

    override suspend fun isReadoutAvailable(): Boolean =
        settingsRepository.isDemoActive().first() || isNotificationAccessGranted()

    override suspend fun markRead(ids: Collection<String>) {
        if (settingsRepository.isDemoActive().first()) demo.markRead(ids) else inbox.markRead(ids)
    }

    private fun isNotificationAccessGranted(): Boolean =
        context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)
}
