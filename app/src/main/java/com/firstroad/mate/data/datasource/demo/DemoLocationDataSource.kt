package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.data.datasource.location.LocationDataSource
import com.firstroad.mate.domain.model.GeoPoint
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoLocationDataSource @Inject constructor(
    private val tripState: DemoTripState,
) : LocationDataSource {

    override fun locationUpdates(): Flow<GeoPoint> = flow {
        while (currentCoroutineContext().isActive) {
            emit(DemoTripScript.locationAt(tripState.elapsedSeconds(System.currentTimeMillis())))
            delay(TICK_MILLIS)
        }
    }

    override suspend fun lastKnownLocation(): GeoPoint =
        DemoTripScript.locationAt(tripState.elapsedSeconds(System.currentTimeMillis()))

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}
