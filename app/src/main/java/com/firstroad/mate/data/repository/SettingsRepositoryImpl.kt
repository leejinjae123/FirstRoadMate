package com.firstroad.mate.data.repository

import com.firstroad.mate.data.datasource.PleosServiceProbe
import com.firstroad.mate.data.datasource.settings.SettingsDataSource
import com.firstroad.mate.domain.model.DataSourceMode
import com.firstroad.mate.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
    private val probe: PleosServiceProbe,
) : SettingsRepository {

    override fun dataSourceMode(): Flow<DataSourceMode> = settingsDataSource.dataSourceMode()

    override suspend fun setDataSourceMode(mode: DataSourceMode) = settingsDataSource.setDataSourceMode(mode)

    override fun isDemoActive(): Flow<Boolean> = settingsDataSource.dataSourceMode()
        .map { mode ->
            when (mode) {
                DataSourceMode.DEMO -> true
                DataSourceMode.VEHICLE -> false
                DataSourceMode.AUTO -> !probe.isPleosAvailable()
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
}
