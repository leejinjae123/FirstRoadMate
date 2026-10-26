package com.firstroad.mate.data.datasource.vehicle.pleos

import ai.pleos.playground.vehicle.api.model.area.WindowArea
import ai.pleos.playground.vehicle.api.model.telltale.Telltales
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.area.WindowValue
import ai.pleos.playground.vehicle.constant.telltale.TelltaleState
import ai.pleos.playground.vehicle.constant.telltale.TelltaleType
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.TurnSignal
import com.firstroad.mate.domain.model.VehicleWarning
import com.firstroad.mate.domain.model.WarningType

// Pleos Vehicle SDK 타입 ↔ 도메인 모델 변환. SDK 타입은 이 패키지 밖으로 나가지 않는다.

internal fun CabinArea.toWindowArea(): WindowArea = WindowArea(
    when (this) {
        CabinArea.FRONT_LEFT -> WindowValue.ROW_1_LEFT
        CabinArea.FRONT_RIGHT -> WindowValue.ROW_1_RIGHT
        CabinArea.REAR_LEFT -> WindowValue.ROW_2_LEFT
        CabinArea.REAR_RIGHT -> WindowValue.ROW_2_RIGHT
    },
)

internal fun TurnSignalValue?.toDomain(): TurnSignal = when (this) {
    TurnSignalValue.LEFT -> TurnSignal.LEFT
    TurnSignalValue.RIGHT -> TurnSignal.RIGHT
    else -> TurnSignal.OFF
}

/** 켜져 있는(ON·BLINK) 경고등 중 운전자에게 설명할 것만 도메인 경고로 바꾼다. */
internal fun Telltales.toWarnings(): List<VehicleWarning> = telltales
    .filter { it.state != TelltaleState.OFF }
    .mapNotNull { telltale ->
        when (telltale.type) {
            TelltaleType.TPMS -> WarningType.TIRE_PRESSURE_LOW
            TelltaleType.LOW_FUEL -> WarningType.LOW_FUEL
            TelltaleType.LOW_EV_BATTERY -> WarningType.LOW_BATTERY
            TelltaleType.ENGINE_TEMPERATURE -> WarningType.ENGINE_OVERHEAT
            TelltaleType.ENGINE_OIL_PRESSURE -> WarningType.OIL_PRESSURE
            TelltaleType.ABS -> WarningType.BRAKE_SYSTEM
            TelltaleType.AIRBAG -> WarningType.AIRBAG
            TelltaleType.MASTER_WARNING -> WarningType.ENGINE_CHECK
            else -> null
        }
    }
    .distinct()
    .map { VehicleWarning(it) }

/** 주차 브레이크는 Brake API가 없어 경고등으로 판단한다. 경고등 정보가 없으면 null. */
internal fun Telltales.isParkingBrakeOn(): Boolean? {
    val brake = telltales.firstOrNull {
        it.type == TelltaleType.PARKING_BRAKE || it.type == TelltaleType.ELECTRONIC_PARKING_BRAKE
    } ?: return null
    return brake.state != TelltaleState.OFF
}

/** SDK 예외를 제어 결과로 바꾼다. 예외 클래스가 공개 API에 없어 메시지로 구분한다. */
internal fun Throwable.toControlResult(): ControlResult {
    val text = toString().uppercase()
    return when {
        this is SecurityException || "PERMISSION" in text || "DRIVING_RESTRICTED" in text -> ControlResult.NOT_PERMITTED
        "NOT_SUPPORTED" in text -> ControlResult.NOT_SUPPORTED
        else -> ControlResult.FAILED
    }
}
