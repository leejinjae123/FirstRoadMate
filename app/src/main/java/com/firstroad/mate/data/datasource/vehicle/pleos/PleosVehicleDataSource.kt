package com.firstroad.mate.data.datasource.vehicle.pleos

import com.firstroad.mate.data.datasource.vehicle.VehicleDataSource
import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.Gear
import com.firstroad.mate.domain.model.TurnSignal
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.VehicleWarning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/** 실차(Pleos Connect) 차량 소스: Pleos Vehicle SDK + AAOS 표준 API를 하나의 스냅숏으로 합친다. */
@Singleton
class PleosVehicleDataSource @Inject constructor(
    private val signals: PleosVehicleSignals,
    private val controls: PleosVehicleControls,
    private val car: AaosCarSignals,
) : VehicleDataSource {

    override fun snapshots(): Flow<VehicleSnapshot> {
        val motion = combine(
            car.speedKmh().onStart { emit(null) },
            car.gear().onStart { emit(Gear.UNKNOWN) },
            signals.turnSignal().onStart { emit(TurnSignal.OFF) },
            signals.brakePressed().onStart { emit(null) },
        ) { speed, gear, signal, brake -> Motion(speed, gear, signal, brake) }

        val energy = combine(
            signals.fuelPercent().onStart { emit(null) },
            signals.evBatteryPercent().onStart { emit(null) },
            signals.wiperOn().onStart { emit(null) },
            signals.headlightOn().onStart { emit(null) },
        ) { fuel, battery, wiper, headlight -> Environment(fuel, battery, wiper, headlight) }

        val body = combine(
            signals.openWindows().onStart { emit(null) },
            signals.telltales().onStart { emit(null) },
        ) { windows, telltales ->
            Body(windows, telltales?.isParkingBrakeOn(), telltales?.toWarnings().orEmpty())
        }

        return combine(motion, energy, body) { m, e, b ->
            VehicleSnapshot(
                speedKmh = m.speedKmh,
                gear = m.gear,
                turnSignal = m.turnSignal,
                isBrakePressed = m.brakePressed,
                isParkingBrakeOn = b.parkingBrakeOn,
                fuelPercent = e.fuelPercent,
                evBatteryPercent = e.batteryPercent,
                openWindows = b.openWindows,
                isHeadlightOn = e.headlightOn,
                isWiperOn = e.wiperOn,
                warnings = b.warnings,
            )
        }
    }

    override fun drivingRestricted(): Flow<Boolean> = car.drivingRestricted()

    override suspend fun closeWindow(area: CabinArea): ControlResult = controls.closeWindow(area)

    override suspend fun applyCabinAdjustment(adjustment: CabinAdjustment): ControlResult =
        controls.applyCabinAdjustment(adjustment)

    override suspend fun restoreCabin(): ControlResult = controls.restoreCabin()

    private data class Motion(val speedKmh: Float?, val gear: Gear, val turnSignal: TurnSignal, val brakePressed: Boolean?)

    private data class Environment(val fuelPercent: Int?, val batteryPercent: Int?, val wiperOn: Boolean?, val headlightOn: Boolean?)

    private data class Body(
        val openWindows: Set<CabinArea>?,
        val parkingBrakeOn: Boolean?,
        val warnings: List<VehicleWarning>,
    )
}
