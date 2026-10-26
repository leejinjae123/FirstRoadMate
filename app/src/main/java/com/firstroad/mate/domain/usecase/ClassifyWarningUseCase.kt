package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.VehicleWarning
import com.firstroad.mate.domain.model.WarningExplanation
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WarningType
import javax.inject.Inject

/**
 * ④ 경고등 통역사: 경고를 3단계 위험 등급으로 분류한다.
 * 안전을 위해 보수적으로 분류한다: 모르는 경고등은 '참고'가 아니라 '세운 뒤 확인'이고,
 * 바로 정차 등급은 정비소가 아니라 가장 가까운 쉴 곳을 제안한다.
 * 안내는 차량 취급설명서를 대체하지 않는다.
 */
class ClassifyWarningUseCase @Inject constructor() {

    operator fun invoke(warning: VehicleWarning): WarningExplanation {
        val (level, stop) = when (warning.type) {
            WarningType.ENGINE_OVERHEAT,
            WarningType.OIL_PRESSURE,
            WarningType.BRAKE_SYSTEM -> WarningLevel.STOP_NOW to PlaceCategory.REST_AREA

            WarningType.UNKNOWN -> WarningLevel.CHECK_TODAY to PlaceCategory.REST_AREA
            WarningType.ENGINE_CHECK,
            WarningType.AIRBAG -> WarningLevel.CHECK_TODAY to PlaceCategory.REPAIR_SHOP
            // 공기압은 휴게소·주유소에서 확인할 수 있다. 고속도로에서도 가는 길에 들르기 쉬운 휴게소를 제안한다.
            WarningType.TIRE_PRESSURE_LOW -> WarningLevel.CHECK_TODAY to PlaceCategory.REST_AREA
            WarningType.LOW_FUEL -> WarningLevel.CHECK_TODAY to PlaceCategory.GAS_STATION
            WarningType.LOW_BATTERY -> WarningLevel.CHECK_TODAY to PlaceCategory.EV_CHARGER
        }
        return WarningExplanation(warning, level, suggestedStop = stop)
    }

    /** 여러 경고 중 가장 위험한 것. 경고가 없으면 null. */
    fun mostSevere(warnings: List<VehicleWarning>): WarningExplanation? =
        warnings.map(::invoke).minByOrNull { it.level.ordinal }
}
