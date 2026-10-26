package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun locationUpdates(): Flow<GeoPoint>

    suspend fun lastKnownLocation(): GeoPoint?
}
