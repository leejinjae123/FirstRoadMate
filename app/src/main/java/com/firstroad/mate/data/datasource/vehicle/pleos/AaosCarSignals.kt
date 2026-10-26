package com.firstroad.mate.data.datasource.vehicle.pleos

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.firstroad.mate.domain.model.Gear
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pleos Vehicle SDK가 제공하지 않는 차속·기어·주행 제한은 Pleos Connect의 기반인
 * Android Automotive 표준 API(CarPropertyManager, CarUxRestrictionsManager)로 읽는다.
 * 자동차가 아닌 기기에서는 모름(null) 또는 제한 없음을 내보낸다.
 */
@Singleton
class AaosCarSignals @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var car: Car? = null

    @Synchronized
    private fun carOrNull(): Car? {
        if (car != null) return car
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) return null
        car = runCatching { Car.createCar(context) }
            .onFailure { Log.w(TAG, "Car 서비스 연결 실패", it) }
            .getOrNull()
        return car
    }

    fun speedKmh(): Flow<Float?> = property(VehiclePropertyIds.PERF_VEHICLE_SPEED) { value ->
        (value as? Float)?.let { it * METERS_PER_SECOND_TO_KMH }
    }

    fun gear(): Flow<Gear> =
        property(VehiclePropertyIds.GEAR_SELECTION) { value -> (value as? Int)?.toGear() }
            .map { it ?: Gear.UNKNOWN }

    fun drivingRestricted(): Flow<Boolean> {
        val manager = carOrNull()?.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
            ?: return flowOf(false)
        return callbackFlow {
            trySend(manager.currentCarUxRestrictions?.isRequiresDistractionOptimization ?: false)
            manager.registerListener { restrictions ->
                trySend(restrictions.isRequiresDistractionOptimization)
            }
            awaitClose { manager.unregisterListener() }
        }.distinctUntilChanged()
    }

    // 대체 API(subscribePropertyEvents)는 최신 플랫폼에만 있어 minSdk 28을 위해 기존 API를 쓴다.
    @Suppress("DEPRECATION")
    private fun <T> property(propertyId: Int, convert: (Any?) -> T?): Flow<T?> {
        val manager = carOrNull()?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            ?: return flowOf(null)
        return callbackFlow {
            val callback = object : CarPropertyManager.CarPropertyEventCallback {
                override fun onChangeEvent(value: CarPropertyValue<*>) {
                    trySend(convert(value.value))
                }

                override fun onErrorEvent(propertyId: Int, areaId: Int) {
                    trySend(null)
                }
            }
            val registered = runCatching {
                manager.registerCallback(callback, propertyId, CarPropertyManager.SENSOR_RATE_NORMAL)
            }.getOrDefault(false)
            if (!registered) trySend(null)
            awaitClose { manager.unregisterCallback(callback) }
        }.distinctUntilChanged()
    }

    private fun Int.toGear(): Gear = when (this) {
        GEAR_PARK -> Gear.PARK
        GEAR_REVERSE -> Gear.REVERSE
        GEAR_NEUTRAL -> Gear.NEUTRAL
        GEAR_DRIVE -> Gear.DRIVE
        else -> Gear.UNKNOWN
    }

    private companion object {
        const val TAG = "AaosCarSignals"
        const val METERS_PER_SECOND_TO_KMH = 3.6f

        // android.car.VehicleGear 값 (AAOS VHAL 정의)
        const val GEAR_NEUTRAL = 0x0001
        const val GEAR_REVERSE = 0x0002
        const val GEAR_PARK = 0x0004
        const val GEAR_DRIVE = 0x0008
    }
}
