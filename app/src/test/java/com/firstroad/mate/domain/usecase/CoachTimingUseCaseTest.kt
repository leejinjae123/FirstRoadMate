package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.BeginnerLevel
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.TurnSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachTimingUseCaseTest {

    private val useCase = CoachTimingUseCase()
    private val beginner = BeginnerLevel.Initial

    private fun rightTurn(distance: Int, junction: Boolean = false) =
        TurnGuide("t1", TurnDirection.RIGHT, distance, "판교로", junction)

    @Test
    fun `25초·350m보다 멀면 아무 안내도 하지 않는다`() {
        // 60km/h = 16.7m/s, 600m → 36초
        assertTrue(useCase(rightTurn(600), 60f, TurnSignal.OFF, beginner).isEmpty())
    }

    @Test
    fun `일반 내비의 300m 안내보다 먼저 차선 준비를 안내한다 (시속 50km, 340m)`() {
        val decisions = useCase(rightTurn(340), 50f, TurnSignal.OFF, beginner)
        assertEquals(listOf("t1:prepare"), decisions.map { it.key })
        assertTrue(decisions.single().guidance is Guidance.PrepareLane)
    }

    @Test
    fun `고속도로 분기는 1km 전에 차선 준비를 안내한다`() {
        val decisions = useCase(rightTurn(900, junction = true), 90f, TurnSignal.OFF, beginner)
        assertEquals(listOf("t1:prepare"), decisions.map { it.key })
    }

    @Test
    fun `7초 안인데 깜빡이가 꺼져 있으면 알린다`() {
        val decisions = useCase(rightTurn(100), 60f, TurnSignal.OFF, beginner)
        assertTrue(decisions.any { it.guidance == Guidance.SignalReminder(TurnDirection.RIGHT) })
    }

    @Test
    fun `3초 안에서 깜빡이를 켰으면 사용으로 기록하고 칭찬한다`() {
        val judge = useCase(rightTurn(40), 60f, TurnSignal.RIGHT, beginner).single { it.key == "t1:judge" }
        assertTrue(judge.countsAsSignalUsed)
        assertEquals(Guidance.SignalPraise, judge.guidance)
    }

    @Test
    fun `3초 안에서도 깜빡이가 꺼져 있으면 놓친 것으로 기록한다`() {
        val judge = useCase(rightTurn(40), 60f, TurnSignal.OFF, beginner).single { it.key == "t1:judge" }
        assertTrue(judge.countsAsSignalMissed)
    }

    @Test
    fun `속도를 모르면 거리 기준으로 판단한다`() {
        val decisions = useCase(rightTurn(300), null, TurnSignal.OFF, beginner)
        assertEquals(listOf("t1:prepare"), decisions.map { it.key })
    }

    @Test
    fun `직진은 코칭하지 않는다`() {
        val straight = TurnGuide("s", TurnDirection.STRAIGHT, 30, null, false)
        assertTrue(useCase(straight, 60f, TurnSignal.OFF, beginner).isEmpty())
    }
}
