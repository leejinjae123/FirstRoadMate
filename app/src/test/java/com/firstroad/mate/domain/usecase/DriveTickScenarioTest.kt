package com.firstroad.mate.domain.usecase

import com.firstroad.mate.data.datasource.demo.DemoTripScript
import com.firstroad.mate.data.datasource.demo.DemoTripState
import com.firstroad.mate.domain.model.BeginnerLevel
import com.firstroad.mate.domain.model.DriveEvent
import com.firstroad.mate.domain.model.DriveInput
import com.firstroad.mate.domain.model.DriveSessionState
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WarningType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 교차 검증: 데모 시나리오(기획서 5장 '운전 2주 차 출근') 120초를 0.5초 간격으로 DriveTickUseCase에 흘려 보내,
 * 기획서에 적은 사건이 실제로 일어나는지 확인한다.
 */
class DriveTickScenarioTest {

    private val driveTick = DriveTickUseCase(
        CoachTimingUseCase(),
        DetectHardBrakeUseCase(),
        ClassifyWarningUseCase(),
        TensionScoreUseCase(),
        BuildRouteBriefingUseCase(),
    )
    private val events = mutableListOf<Pair<Float, DriveEvent>>()
    private lateinit var finalState: DriveSessionState

    @Before
    fun runScenario() {
        val trip = DemoTripState.State(startedAtMillis = 0L)
        var state = DriveSessionState()
        var t = 0f
        while (t <= 120f && !state.isFinished) {
            val input = DriveInput(
                vehicle = DemoTripScript.vehicleAt(t, trip),
                navigation = DemoTripScript.navigationAt(t, trip),
                level = BeginnerLevel.Initial,
                nowMillis = (t * 1000).toLong(),
                location = DemoTripScript.locationAt(t),
                destination = DemoTripScript.DESTINATION,
                difficultSpots = emptyList(),
            )
            val tick = driveTick(state, input)
            tick.events.forEach { events += t to it }
            state = tick.state
            t += 0.5f
        }
        finalState = state
    }

    private fun said(): List<Guidance> = events.mapNotNull { (it.second as? DriveEvent.Say)?.guidance }

    private fun firstAt(predicate: (DriveEvent) -> Boolean): Float = events.first { predicate(it.second) }.first

    @Test
    fun `출발하면 브리핑을 먼저 하고, 첫 갈림길의 방향까지 예습시킨다`() {
        val first = said().first() as Guidance.RouteBriefing
        assertEquals(2, first.highwayJunctions)
        assertEquals(TurnDirection.RIGHT, first.firstJunction?.direction)
    }

    @Test
    fun `합류 1km 전에 차선 준비를 안내하고, 깜빡이를 늦게 켜면 먼저 알린 뒤 칭찬한다`() {
        val prepareAt = firstAt { (it as? DriveEvent.Say)?.guidance is Guidance.PrepareLane }
        assertTrue("브리핑과 겹치지 않게 10초 이후", prepareAt >= 10f)
        val guidances = said()
        val reminderIndex = guidances.indexOf(Guidance.SignalReminder(TurnDirection.RIGHT))
        val praiseIndex = guidances.indexOf(Guidance.SignalPraise)
        assertTrue("알림 후 칭찬", reminderIndex in 0 until praiseIndex)
    }

    @Test
    fun `깜빡이 3번 중 2번 사용, 1번 놓침으로 기록한다`() {
        assertEquals(2, finalState.stats.signalUsed)
        assertEquals(1, finalState.stats.signalMissed)
    }

    @Test
    fun `45초 타이어 경고는 오늘 중 점검 등급이고 정비소 경유를 묻는다`() {
        val warnings = said().filterIsInstance<Guidance.WarningNotice>()
        assertEquals(listOf(Guidance.WarningNotice(WarningType.TIRE_PRESSURE_LOW, WarningLevel.CHECK_TODAY)), warnings)
        assertTrue(firstAt { it is DriveEvent.ShowWarning } in 45f..46f)
        val ask = events.map { it.second }.filterIsInstance<DriveEvent.Ask>().single()
        assertEquals(Guidance.AskAddWaypoint(PlaceCategory.REPAIR_SHOP), ask.guidance)
    }

    @Test
    fun `판교IC도 분기 1km 전에 차선 준비를 안내하고, 미리 켠 깜빡이를 칭찬한다`() {
        // 합류(37초) 이후의 첫 고속도로 차선 준비 안내 = 판교IC
        val exitPrepare = events.first { (t, e) ->
            val g = (e as? DriveEvent.Say)?.guidance
            t > 37.2f && g is Guidance.PrepareLane && g.isHighway
        }
        val exitGuidance = (exitPrepare.second as DriveEvent.Say).guidance as Guidance.PrepareLane
        assertTrue("판교IC 안내 거리 ${exitGuidance.distanceMeters}", exitGuidance.distanceMeters in 900..1_000)
        assertTrue("판교IC 안내 시각 ${exitPrepare.first}", exitPrepare.first in 38f..40f)
        val praises = events.filter { (it.second as? DriveEvent.Say)?.guidance == Guidance.SignalPraise }.map { it.first }
        assertEquals("칭찬 시각 $praises", 2, praises.size)
    }

    @Test
    fun `하차 때 열려 있는 뒷창문은 출발 직후 연 것이다`() {
        val trip = DemoTripState.State(startedAtMillis = 0L)
        assertTrue(DemoTripScript.vehicleAt(10f, trip).openWindows!!.isNotEmpty())
        assertTrue(DemoTripScript.vehicleAt(114f, trip).openWindows!!.isNotEmpty())
    }

    @Test
    fun `72초 급제동을 한 번 감지하고, 제한속도 변경으로 과속 경고가 나지 않는다`() {
        assertEquals(1, finalState.stats.hardBrakes)
        assertTrue(said().contains(Guidance.HardBrakeTip))
        assertTrue(said().none { it is Guidance.SpeedOver })
    }

    @Test
    fun `합류 전에 진정 모드가 켜지고, 지나면 실내를 되돌리고 잘 지나왔다고 말한다`() {
        assertTrue("합류(37초) 전에 시작", firstAt { it == DriveEvent.StartCalm } < 37f)
        assertTrue(events.any { it.second == DriveEvent.EndCalm })
        assertTrue(said().contains(Guidance.PassedWell))
        assertEquals(1, finalState.stats.difficultPassed)
    }

    @Test
    fun `도착 후 P단에서 주행을 마친다`() {
        assertTrue(said().contains(Guidance.Arrived))
        assertTrue(finalState.isFinished)
        assertTrue(events.any { it.second == DriveEvent.Finished })
    }

    @Test
    fun `차속이 그대로인 다른 신호의 틱은 급제동으로 잡지 않는다`() {
        var state = DriveSessionState(startedAtMillis = 0, isBriefed = true)
        val navigation = NavigationState(isRouteActive = true)
        fun tick(speed: Float, at: Long) {
            state = driveTick(
                state,
                DriveInput(VehicleSnapshot(speedKmh = speed), navigation, BeginnerLevel.Initial, at, null, null, emptyList()),
            ).state
        }
        tick(50f, 0)        // 차속 샘플 (1초 주기)
        tick(50f, 800)      // 내비 이벤트로 생긴 틱: 차속 그대로
        tick(47f, 1_000)    // 다음 차속 샘플: 1초에 3km/h 감속은 급제동이 아니다
        assertEquals(0, state.stats.hardBrakes)
    }
}
