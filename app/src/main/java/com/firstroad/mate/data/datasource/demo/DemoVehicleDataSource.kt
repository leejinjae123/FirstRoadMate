package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.data.datasource.vehicle.VehicleDataSource
import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.Gear
import com.firstroad.mate.domain.model.VehicleSnapshot
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoVehicleDataSource @Inject constructor(
    private val tripState: DemoTripState,
) : VehicleDataSource {

    override fun snapshots(): Flow<VehicleSnapshot> = flow {
        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            emit(DemoTripScript.vehicleAt(tripState.elapsedSeconds(now), tripState.state.value))
            delay(TICK_MILLIS)
        }
    }

    override fun drivingRestricted(): Flow<Boolean> = snapshots()
        .map { it.gear != Gear.PARK && (it.speedKmh ?: 0f) > 0f }
        .distinctUntilChanged()

    override suspend fun closeWindow(area: CabinArea): ControlResult {
        tripState.closeWindow(area)
        return ControlResult.DONE
    }

    override suspend fun applyCabinAdjustment(adjustment: CabinAdjustment): ControlResult {
        tripState.setCabinAdjusted(true)
        return ControlResult.DONE
    }

    override suspend fun restoreCabin(): ControlResult {
        tripState.setCabinAdjusted(false)
        return ControlResult.DONE
    }

    private companion object {
        const val TICK_MILLIS = 500L
    }
}
