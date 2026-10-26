package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.VehicleWarning
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WarningType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** 안전 판단은 보수적이어야 한다. */
class ClassifyWarningUseCaseTest {

    private val classify = ClassifyWarningUseCase()

    @Test
    fun `처음 보는 경고등은 참고가 아니라 세운 뒤 확인 등급이다`() {
        val result = classify(VehicleWarning(WarningType.UNKNOWN))
        assertNotEquals(WarningLevel.INFO, result.level)
        assertEquals(PlaceCategory.REST_AREA, result.suggestedStop)
    }

    @Test
    fun `바로 정차 등급은 정비소가 아니라 가장 가까운 쉴 곳을 제안한다`() {
        WarningType.entries.map { classify(VehicleWarning(it)) }
            .filter { it.level == WarningLevel.STOP_NOW }
            .forEach { assertEquals(PlaceCategory.REST_AREA, it.suggestedStop) }
    }

    @Test
    fun `타이어 공기압 경고는 오늘 중 점검이고 가는 길의 정비소를 제안한다`() {
        val result = classify(VehicleWarning(WarningType.TIRE_PRESSURE_LOW))
        assertEquals(WarningLevel.CHECK_TODAY, result.level)
        assertEquals(PlaceCategory.REPAIR_SHOP, result.suggestedStop)
    }
}
