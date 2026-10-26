package com.firstroad.mate.data.repository

import com.firstroad.mate.data.datasource.DemoSource
import com.firstroad.mate.data.datasource.PleosSource
import com.firstroad.mate.data.datasource.vehicle.VehicleDataSource
import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/** 실차 / 데모 소스 중 하나를 골라 쓴다. 화면은 어느 쪽인지 알 필요가 없다. */
@OptIn(ExperimentalCoroutinesApi::class)
class VehicleRepositoryImpl @Inject constructor(
    @PleosSource private val pleos: VehicleDataSource,
    @DemoSource private val demo: VehicleDataSource,
    private val settingsRepository: SettingsRepository,
) : VehicleRepository {

    override fun vehicleSnapshot(): Flow<VehicleSnapshot> =
        settingsRepository.isDemoActive().flatMapLatest { isDemo -> source(isDemo).snapshots() }

    override fun isDrivingRestricted(): Flow<Boolean> =
        settingsRepository.isDemoActive().flatMapLatest { isDemo -> source(isDemo).drivingRestricted() }

    override suspend fun closeWindow(area: CabinArea): ControlResult = current().closeWindow(area)

    override suspend fun applyCabinAdjustment(adjustment: CabinAdjustment): ControlResult =
        current().applyCabinAdjustment(adjustment)

    override suspend fun restoreCabin(): ControlResult = current().restoreCabin()

    private suspend fun current(): VehicleDataSource = source(settingsRepository.isDemoActive().first())

    private fun source(isDemo: Boolean): VehicleDataSource = if (isDemo) demo else pleos
}
