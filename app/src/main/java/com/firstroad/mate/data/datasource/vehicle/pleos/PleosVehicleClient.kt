package com.firstroad.mate.data.datasource.vehicle.pleos

import ai.pleos.playground.vehicle.Vehicle
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Pleos Vehicle Service 연결을 앱 전체에서 하나로 관리한다. 연결할 수 없으면 null. */
@Singleton
class PleosVehicleClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var vehicle: Vehicle? = null
    private var initializeFailed = false

    @Synchronized
    fun vehicleOrNull(): Vehicle? {
        if (vehicle != null || initializeFailed) return vehicle
        return runCatching { Vehicle(context).also { it.initialize() } }
            .onFailure {
                initializeFailed = true
                Log.w(TAG, "Vehicle SDK 연결 실패: 데모 또는 안내 모드로 동작합니다.", it)
            }
            .getOrNull()
            .also { vehicle = it }
    }

    private companion object {
        const val TAG = "PleosVehicleClient"
    }
}
