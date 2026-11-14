package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.BeginnerLevel
import com.firstroad.mate.domain.model.CautionPoint
import com.firstroad.mate.domain.model.CautionType
import com.firstroad.mate.domain.model.DriveEvent
import com.firstroad.mate.domain.model.DriveInput
import com.firstroad.mate.domain.model.DriveSessionState
import com.firstroad.mate.domain.model.Gear
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.IncomingMessage
import com.firstroad.mate.domain.model.MessengerApp
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.PendingAction
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.VehicleSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ⑦ 메시지 읽어 주기: 새 메시지를 언제 꺼내는지 (조용한 구간·정차 중에만, 한 번만). */
class MessageTimingTest {

    private val timing = MessageTimingUseCase()
    private val driveTick = DriveTickUseCase(
        CoachTimingUseCase(),
        DetectHardBrakeUseCase(),
        ClassifyWarningUseCase(),
        TensionScoreUseCase(),
        BuildRouteBriefingUseCase(),
        timing,
    )

    private val mom = IncomingMessage("m1", "엄마", "저녁 먹고 와?", MessengerApp.KAKAOTALK, 0L)
    private val boss = IncomingMessage("m2", "김 대리", "내일 회의 10시", MessengerApp.SMS, 1L)

    private fun far(direction: TurnDirection = TurnDirection.RIGHT) = TurnGuide("t", direction, 3_000, null, isHighwayJunction = false)

    private fun input(
        at: Long,
        speed: Float = 60f,
        nextTurn: TurnGuide? = far(),
        caution: CautionPoint? = null,
        messages: List<IncomingMessage> = listOf(mom),
        hasOpenQuestion: Boolean = false,
    ) = DriveInput(
        vehicle = VehicleSnapshot(speedKmh = speed, gear = Gear.DRIVE),
        navigation = NavigationState(isRouteActive = true, nextTurn = nextTurn, cautionAhead = caution),
        level = BeginnerLevel.Initial,
        nowMillis = at,
        location = null,
        destination = null,
        difficultSpots = emptyList(),
        messages = messages,
        hasOpenQuestion = hasOpenQuestion,
    )

    /** 0.5초 간격으로 흘려 보내고 모든 이벤트를 모은다. */
    private fun run(
        seconds: Int,
        start: DriveSessionState = DriveSessionState(startedAtMillis = 0, isBriefed = true),
        make: (Long) -> DriveInput,
    ): Pair<DriveSessionState, List<DriveEvent>> {
        var state = start
        val events = mutableListOf<DriveEvent>()
        for (step in 0..seconds * 2) {
            val tick = driveTick(state, make(step * 500L))
            state = tick.state
            events += tick.events
        }
        return state to events
    }

    private fun List<DriveEvent>.messageAsks() = filterIsInstance<DriveEvent.Ask>().filter { it.action == PendingAction.ReadMessages }

    @Test
    fun `조용한 직진이 5초 이어지면 한 번 묻는다`() {
        val (_, events) = run(seconds = 20) { input(it) }
        val asks = events.messageAsks()
        assertEquals(1, asks.size)
        assertEquals(Guidance.AskReadMessages(1, "엄마", 0), asks.single().guidance)
    }

    @Test
    fun `조용해진 지 5초가 안 되면 묻지 않는다`() {
        val (_, events) = run(seconds = 4) { input(it) }
        assertTrue(events.messageAsks().isEmpty())
    }

    @Test
    fun `정차 중(신호 대기)이면 바로 묻는다`() {
        val (_, events) = run(seconds = 1) { input(it, speed = 0f) }
        assertEquals(1, events.messageAsks().size)
    }

    @Test
    fun `회전 준비 구간·주의 지점 앞에서는 묻지 않는다`() {
        val nearTurn = TurnGuide("t", TurnDirection.LEFT, 300, null, isHighwayJunction = false)
        val junction = TurnGuide("j", TurnDirection.RIGHT, 900, null, isHighwayJunction = true)
        val merge = CautionPoint(CautionType.MERGE, 400)
        assertTrue(run(seconds = 20) { input(it, nextTurn = nearTurn) }.second.messageAsks().isEmpty())
        assertTrue(run(seconds = 20) { input(it, nextTurn = junction) }.second.messageAsks().isEmpty())
        assertTrue(run(seconds = 20) { input(it, caution = merge) }.second.messageAsks().isEmpty())
    }

    @Test
    fun `목적지 500m 안에서는 정차 중이어도 묻지 않는다 (하차 화면에서 묻는다)`() {
        val (_, events) = run(seconds = 10) {
            input(it, speed = 0f).let { i -> i.copy(navigation = i.navigation.copy(remainingMeters = 300)) }
        }
        assertTrue(events.messageAsks().isEmpty())
    }

    @Test
    fun `직진 안내는 회전이 아니라서 보류하지 않는다`() {
        val straight = TurnGuide("s", TurnDirection.STRAIGHT, 200, null, isHighwayJunction = false)
        assertEquals(1, run(seconds = 20) { input(it, nextTurn = straight) }.second.messageAsks().size)
    }

    @Test
    fun `다른 질문이 떠 있으면 겹쳐 묻지 않는다`() {
        val (_, events) = run(seconds = 20) { input(it, hasOpenQuestion = true) }
        assertTrue(events.messageAsks().isEmpty())
    }

    @Test
    fun `새 메시지가 와도 1분 안에는 다시 묻지 않고, 1분 뒤에 전체 건수로 묻는다`() {
        val (state, first) = run(seconds = 10) { input(it) }
        assertEquals(1, first.messageAsks().size)
        val start = state.copy(lastMessageAskAtMillis = 0L)
        val (_, again) = run(seconds = 70, start = start) { input(it, messages = listOf(mom, boss)) }
        val asks = again.messageAsks()
        assertEquals(1, asks.size)
        assertEquals(Guidance.AskReadMessages(2, "김 대리", 1), asks.single().guidance)
    }

    @Test
    fun `미뤄 둔 읽기 요청은 조용한 구간이 되면 묻지 않고 바로 읽는다`() {
        val start = DriveSessionState(startedAtMillis = 0, isBriefed = true, isMessageReadDeferred = true)
        val (state, events) = run(seconds = 10, start = start) { input(it) }
        assertEquals(1, events.count { it == DriveEvent.ReadMessages })
        assertTrue(events.messageAsks().isEmpty())
        assertFalse(state.isMessageReadDeferred)
    }

    @Test
    fun `같은 사람에게서 여러 건이면 사람 수가 아니라 건수로 묻는다`() {
        val again = mom.copy(id = "m3", text = "답장해 줘")
        assertEquals(Guidance.AskReadMessages(2, "엄마", 0), Guidance.AskReadMessages.of(listOf(mom, again)))
    }
}
