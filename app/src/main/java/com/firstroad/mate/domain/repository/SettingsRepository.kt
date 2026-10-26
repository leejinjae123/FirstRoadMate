package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.DataSourceMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun dataSourceMode(): Flow<DataSourceMode>

    suspend fun setDataSourceMode(mode: DataSourceMode)

    /** 실제로 쓰고 있는 소스가 데모인지 (AUTO일 때 Pleos 서비스 유무로 결정). */
    fun isDemoActive(): Flow<Boolean>

    /** 긴장 구간 공조 자동 조절 사전 동의 (null = 아직 묻지 않음). */
    fun cabinAdjustConsent(): Flow<Boolean?>

    suspend fun setCabinAdjustConsent(allowed: Boolean)
}
