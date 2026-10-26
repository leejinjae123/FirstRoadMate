package com.firstroad.mate.data.datasource

import android.content.Context
import android.content.pm.PackageManager
import com.firstroad.mate.data.datasource.vehicle.pleos.PleosVehicleClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 자동 모드에서 실차 소스를 쓸지 판단한다: 자동차 기기이고 Pleos Vehicle Service에 연결되면 실차. */
@Singleton
class PleosServiceProbe @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vehicleClient: PleosVehicleClient,
) {
    fun isPleosAvailable(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE) &&
            vehicleClient.vehicleOrNull() != null
}
