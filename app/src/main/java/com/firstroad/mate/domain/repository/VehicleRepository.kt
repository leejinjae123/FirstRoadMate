package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.CabinAdjustment
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.ControlResult
import com.firstroad.mate.domain.model.VehicleSnapshot
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    /** 차량 상태 스트림. 실차 또는 데모 소스에서 온다. 첫 값은 아직 읽지 못한 항목이 null일 수 있다. */
    fun vehicleSnapshot(): Flow<VehicleSnapshot>

    /** 주행 중 화면 제한(CarUxRestrictions) 여부 스트림. */
    fun isDrivingRestricted(): Flow<Boolean>

    suspend fun closeWindow(area: CabinArea): ControlResult

    /** 카밍 캐빈 조절. 이미 조절 중이면 다시 쌓지 않는다. */
    suspend fun applyCabinAdjustment(adjustment: CabinAdjustment): ControlResult

    /** 카밍 캐빈 조절 전의 값으로 되돌린다. */
    suspend fun restoreCabin(): ControlResult
}
