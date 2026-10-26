package com.firstroad.mate.data.datasource.vehicle

import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.VehicleSnapshot
import kotlinx.coroutines.flow.Flow

interface VehicleDataSource {
    fun snapshots(): Flow<VehicleSnapshot>

    fun drivingRestricted(): Flow<Boolean>

    suspend fun closeWindow(area: CabinArea): ControlResult

    suspend fun applyCabinAdjustment(adjustment: CabinAdjustment): ControlResult

    suspend fun restoreCabin(): ControlResult
}
