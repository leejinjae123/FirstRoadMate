package com.firstroad.mate.data.datasource.location

import com.firstroad.mate.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    fun locationUpdates(): Flow<GeoPoint>

    suspend fun lastKnownLocation(): GeoPoint?
}
