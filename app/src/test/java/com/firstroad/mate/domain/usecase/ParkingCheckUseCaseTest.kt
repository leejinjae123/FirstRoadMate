package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParkingCheckUseCaseTest {

    private val check = ParkingCheckUseCase()

    @Test
    fun `열린 창문은 문제 항목으로 앞에 오고 원격으로 고칠 수 있다`() {
        val items = check(VehicleSnapshot(openWindows = setOf(CabinArea.REAR_LEFT), isHeadlightOn = false))
        val first = items.first()
        assertEquals(CheckType.WINDOW, first.type)
        assertEquals(CabinArea.REAR_LEFT, first.area)
        assertFalse(first.isOk)
        assertTrue(first.canFixRemotely)
    }

    @Test
    fun `읽을 수 없는 항목(null)은 점검하지 않는다`() {
        val items = check(VehicleSnapshot(isHeadlightOn = true))
        assertEquals(listOf(CheckType.HEADLIGHT), items.map { it.type })
    }

    @Test
    fun `아직 아무것도 읽지 못한 첫 스냅숏은 점검 항목이 없다 (모두 괜찮다고 말하면 안 됨)`() {
        assertTrue(check(VehicleSnapshot()).isEmpty())
    }

    @Test
    fun `모두 괜찮으면 문제 항목이 없다`() {
        val items = check(VehicleSnapshot(openWindows = emptySet(), isHeadlightOn = false, isParkingBrakeOn = true))
        assertTrue(items.all { it.isOk })
    }
}
