package com.firstroad.mate.domain.model

/**
 * 한 시점의 차량 상태. 읽을 수 없는 값(차종 미지원, 권한 없음, SDK 미제공)은 null이다.
 * null은 "문제없음"이 아니라 "모름"이므로, 점검 로직은 null 항목을 건너뛴다.
 */
data class VehicleSnapshot(
    val speedKmh: Float? = null,
    val gear: Gear = Gear.UNKNOWN,
    val turnSignal: TurnSignal = TurnSignal.OFF,
    val isBrakePressed: Boolean? = null,
    val isParkingBrakeOn: Boolean? = null,
    val fuelPercent: Int? = null,
    val evBatteryPercent: Int? = null,
    val openWindows: Set<CabinArea>? = null,
    val isHeadlightOn: Boolean? = null,
    val isWiperOn: Boolean? = null,
    val warnings: List<VehicleWarning> = emptyList(),
) {
    val isParked: Boolean get() = gear == Gear.PARK

    /** 연료와 배터리 중 낮은 쪽. 둘 다 모르면 null. */
    val energyPercent: Int?
        get() = listOfNotNull(fuelPercent, evBatteryPercent).minOrNull()
}
