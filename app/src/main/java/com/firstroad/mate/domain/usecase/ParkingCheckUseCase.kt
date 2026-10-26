package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.ParkingCheckItem
import com.firstroad.mate.domain.model.VehicleSnapshot
import javax.inject.Inject

/**
 * ⑥ 하차 안심 체크 (시동 끄기 전 점검). 읽을 수 있는 항목만 점검하고(null은 모름 → 건너뜀),
 * 문제 항목을 앞에 둔다.
 */
class ParkingCheckUseCase @Inject constructor() {

    operator fun invoke(vehicle: VehicleSnapshot): List<ParkingCheckItem> {
        val items = mutableListOf<ParkingCheckItem>()

        vehicle.openWindows?.let { open ->
            if (open.isEmpty()) {
                items += ParkingCheckItem(CheckType.WINDOW, area = null, isOk = true, canFixRemotely = false)
            } else {
                open.sorted().forEach { area ->
                    items += ParkingCheckItem(CheckType.WINDOW, area, isOk = false, canFixRemotely = true)
                }
            }
        }
        vehicle.isHeadlightOn?.let { on ->
            items += ParkingCheckItem(CheckType.HEADLIGHT, area = null, isOk = !on, canFixRemotely = false)
        }
        vehicle.isParkingBrakeOn?.let { on ->
            items += ParkingCheckItem(CheckType.PARKING_BRAKE, area = null, isOk = on, canFixRemotely = false)
        }
        return items.sortedBy { it.isOk }
    }
}
