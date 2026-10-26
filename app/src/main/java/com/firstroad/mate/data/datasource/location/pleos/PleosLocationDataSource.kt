package com.firstroad.mate.data.datasource.location.pleos

import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.location.fused.listener.FusedLocationListener
import ai.pleos.playground.location.fused.model.LocationData
import android.content.Context
import com.firstroad.mate.data.datasource.location.LocationDataSource
import com.firstroad.mate.domain.model.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.firstroad.mate.data.datasource.awaitSdkCallback
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PleosLocationDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationDataSource {

    private var fusedLocation: FusedLocation? = null
    private var unsupported = false

    @Synchronized
    private fun clientOrNull(): FusedLocation? {
        if (fusedLocation != null || unsupported) return fusedLocation
        val candidate = runCatching { FusedLocation(context) }.getOrNull()
        val ready = candidate != null && runCatching { candidate.isSupported() }.getOrDefault(false) &&
            runCatching { candidate.initialize() }.isSuccess
        if (ready) fusedLocation = candidate else unsupported = true
        return fusedLocation
    }

    override fun locationUpdates(): Flow<GeoPoint> {
        val client = clientOrNull() ?: return emptyFlow()
        return callbackFlow {
            val listener = object : FusedLocationListener {
                override fun onChanged(location: LocationData?) {
                    location?.let { trySend(GeoPoint(it.latitude, it.longitude)) }
                }

                override fun onError(exception: Exception) = Unit
            }
            runCatching { client.registerFusedLocationCallback(listener) }
            awaitClose { runCatching { client.unregisterFusedLocationCallback(listener) } }
        }
    }

    override suspend fun lastKnownLocation(): GeoPoint? {
        val client = clientOrNull() ?: return null
        return awaitSdkCallback { resume ->
            client.getLastKnownLocation(object : FusedLocationListener {
                override fun onChanged(location: LocationData?) = resume(location?.let { GeoPoint(it.latitude, it.longitude) })
                override fun onError(exception: Exception) = resume(null)
            })
        }
    }
}
