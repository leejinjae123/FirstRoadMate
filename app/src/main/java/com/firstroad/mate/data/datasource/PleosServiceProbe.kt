package com.firstroad.mate.data.datasource

import android.content.Context
import android.content.pm.PackageManager
import com.firstroad.mate.data.datasource.vehicle.pleos.PleosVehicleClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 자동 모드에서 실차 소스를 쓸지 판단한다: 자동차 기기이고, Pleos 플랫폼 권한이 정의되어 있고, Vehicle Service에 연결되면 실차.
 * SDK 객체는 Pleos가 없는 일반 AAOS에서도 만들어지므로, 권한 정의 여부로 Pleos 플랫폼인지 확인한다.
 */
@Singleton
class PleosServiceProbe @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vehicleClient: PleosVehicleClient,
) {
    fun isPleosAvailable(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE) &&
            isPleosPlatform() &&
            vehicleClient.vehicleOrNull() != null

    private fun isPleosPlatform(): Boolean {
        val packageManager = context.packageManager
        val requested = runCatching {
            packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
        }.getOrNull().orEmpty()
        return requested.filter { it.startsWith(PLEOS_PERMISSION_PREFIX) }
            .any { runCatching { packageManager.getPermissionInfo(it, 0) }.isSuccess }
    }

    private companion object {
        const val PLEOS_PERMISSION_PREFIX = "pleos.car.permission."
    }
}
