package com.firstroad.mate.data.datasource.vehicle.pleos

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.constant.area.SeatValue
import ai.pleos.playground.vehicle.constant.unit.TemperatureUnit
import com.firstroad.mate.data.datasource.awaitSdkCallback
import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 차량 제어 요청. 권한·차종 지원 여부에 따라 NOT_PERMITTED / NOT_SUPPORTED를 돌려주고,
 * 호출하는 쪽은 그 경우 '안내 모드'로 대체한다.
 * 카밍 캐빈은 조절 전 값을 기억해 두었다가 [restoreCabin]에서 되돌린다 (반복해도 누적되지 않음).
 */
@Singleton
class PleosVehicleControls @Inject constructor(
    private val client: PleosVehicleClient,
) {
    private val cabinMutex = Mutex()
    private var originalCabin: CabinValues? = null

    suspend fun closeWindow(area: CabinArea): ControlResult {
        val vehicle = client.vehicleOrNull() ?: return ControlResult.NOT_SUPPORTED
        return awaitSdkCallback<ControlResult> { resume ->
            vehicle.getWindow().setWindowPosition(
                area.toWindowArea(),
                WINDOW_CLOSED_POSITION,
                { _, _ -> resume(ControlResult.DONE) },
                { error -> resume(error.toControlResult()) },
            )
        } ?: ControlResult.FAILED
    }

    suspend fun applyCabinAdjustment(adjustment: CabinAdjustment): ControlResult = cabinMutex.withLock {
        val vehicle = client.vehicleOrNull() ?: return@withLock ControlResult.NOT_SUPPORTED
        if (originalCabin != null) return@withLock ControlResult.DONE
        val temperature = readTemperature(vehicle)
        val fan = readFanSpeed(vehicle)
        if (temperature == null && fan == null) return@withLock ControlResult.NOT_SUPPORTED
        val results = listOfNotNull(
            temperature?.let { setTemperature(vehicle, it + adjustment.temperatureDeltaCelsius) },
            fan?.let { setFanSpeed(vehicle, it + adjustment.fanSpeedDelta) },
        )
        val done = results.any { it == ControlResult.DONE }
        if (done) originalCabin = CabinValues(temperature, fan)
        if (done) ControlResult.DONE else results.first()
    }

    suspend fun restoreCabin(): ControlResult = cabinMutex.withLock {
        val vehicle = client.vehicleOrNull() ?: return@withLock ControlResult.NOT_SUPPORTED
        val original = originalCabin ?: return@withLock ControlResult.DONE
        originalCabin = null
        val results = listOfNotNull(
            original.temperature?.let { setTemperature(vehicle, it) },
            original.fanSpeed?.let { setFanSpeed(vehicle, it) },
        )
        if (results.all { it == ControlResult.DONE }) ControlResult.DONE else ControlResult.FAILED
    }

    private suspend fun readTemperature(vehicle: Vehicle): Float? = awaitSdkCallback { resume ->
        vehicle.getHVAC().getHvacTemperature(DRIVER_SEAT, TemperatureUnit.CELSIUS, { resume(it) }, { resume(null) })
    }

    private suspend fun readFanSpeed(vehicle: Vehicle): Int? = awaitSdkCallback { resume ->
        vehicle.getHVAC().getHvacFanSpeed(DRIVER_SEAT, { resume(it) }, { resume(null) })
    }

    private suspend fun setTemperature(vehicle: Vehicle, celsius: Float): ControlResult =
        awaitSdkCallback<ControlResult> { resume ->
            vehicle.getHVAC().setHvacTemperature(
                DRIVER_SEAT,
                celsius,
                TemperatureUnit.CELSIUS,
                { _, _ -> resume(ControlResult.DONE) },
                { error -> resume(error.toControlResult()) },
            )
        } ?: ControlResult.FAILED

    private suspend fun setFanSpeed(vehicle: Vehicle, speed: Int): ControlResult =
        awaitSdkCallback<ControlResult> { resume ->
            vehicle.getHVAC().setHvacFanSpeed(
                DRIVER_SEAT,
                speed,
                { _, _ -> resume(ControlResult.DONE) },
                { error -> resume(error.toControlResult()) },
            )
        } ?: ControlResult.FAILED

    private data class CabinValues(val temperature: Float?, val fanSpeed: Int?)

    private companion object {
        const val WINDOW_CLOSED_POSITION = 0
        val DRIVER_SEAT = SeatArea(SeatValue.ROW_1_LEFT)
    }
}
