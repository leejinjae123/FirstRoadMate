package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.DriveRecord
import com.firstroad.mate.domain.model.DriveStats
import com.firstroad.mate.domain.model.LevelTier
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateLevelUseCaseTest {

    private val calculate = CalculateLevelUseCase()

    private fun records(count: Int, used: Int, missed: Int, hardBrakes: Int = 0) = List(count) {
        DriveRecord(startedAtMillis = 0, endedAtMillis = 1, destinationName = "x", stats = DriveStats(used, missed, hardBrakes))
    }

    @Test
    fun `처음에는 새싹`() = assertEquals(LevelTier.SPROUT, calculate(emptyList()).tier)

    @Test
    fun `3번 주행하면 연습생`() = assertEquals(LevelTier.TRAINEE, calculate(records(3, 1, 1)).tier)

    @Test
    fun `10번 주행해도 깜빡이 준수율이 낮으면 3단계로 오르지 않는다`() =
        assertEquals(LevelTier.TRAINEE, calculate(records(10, 1, 1)).tier)

    @Test
    fun `10번 주행에 깜빡이 80퍼센트 이상이면 도로 적응`() =
        assertEquals(LevelTier.ADAPTING, calculate(records(10, 4, 1)).tier)
}
