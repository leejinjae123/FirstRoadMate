package com.firstroad.mate.data.datasource.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.firstroad.mate.domain.model.DataSourceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** DataStore 기반 설정. 전역 변수 대신 DI로 주입한다. */
@Singleton
class SettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    fun dataSourceMode(): Flow<DataSourceMode> = dataStore.data.map { prefs ->
        prefs[KEY_DATA_SOURCE_MODE]
            ?.let { saved -> DataSourceMode.entries.firstOrNull { it.name == saved } }
            ?: DataSourceMode.AUTO
    }

    suspend fun setDataSourceMode(mode: DataSourceMode) {
        dataStore.edit { it[KEY_DATA_SOURCE_MODE] = mode.name }
    }

    /** 긴장 구간 공조 자동 조절 사전 동의. 아직 묻지 않았으면 null. */
    fun cabinAdjustConsent(): Flow<Boolean?> = dataStore.data.map { it[KEY_CABIN_CONSENT] }

    suspend fun setCabinAdjustConsent(allowed: Boolean) {
        dataStore.edit { it[KEY_CABIN_CONSENT] = allowed }
    }

    private companion object {
        val KEY_DATA_SOURCE_MODE = stringPreferencesKey("data_source_mode")
        val KEY_CABIN_CONSENT = booleanPreferencesKey("cabin_adjust_consent")
    }
}
