package com.firstroad.mate.data.repository

import com.firstroad.mate.data.datasource.DemoSource
import com.firstroad.mate.data.datasource.PleosSource
import com.firstroad.mate.data.datasource.location.LocationDataSource
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.repository.LocationRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class LocationRepositoryImpl @Inject constructor(
    @PleosSource private val pleos: LocationDataSource,
    @DemoSource private val demo: LocationDataSource,
    private val settingsRepository: SettingsRepository,
) : LocationRepository {

    override fun locationUpdates(): Flow<GeoPoint> =
        settingsRepository.isDemoActive().flatMapLatest { isDemo -> source(isDemo).locationUpdates() }

    override suspend fun lastKnownLocation(): GeoPoint? =
        source(settingsRepository.isDemoActive().first()).lastKnownLocation()

    private fun source(isDemo: Boolean): LocationDataSource = if (isDemo) demo else pleos
}
