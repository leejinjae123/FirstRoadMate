package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CautionPoint
import com.firstroad.mate.domain.model.CautionType
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Gear
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.TurnSignal
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.VehicleWarning
import com.firstroad.mate.domain.model.WarningType
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 기획서 5장 '운전 2주 차 출근'(수원 영통 → 판교, 실제 약 40분)을 120초로 압축한 데모 시나리오 (상태 없는 순수 함수).
 *
 * - 5초 더워서 뒷좌석 왼쪽 창문을 엶 (하차 때까지 잊음)
 * - 0~37초 경부고속도로 합류: 1km 전 차선 준비 안내, 깜빡이를 늦게 켜서(35.5초) 알림 후 칭찬, 합류 전 진정 모드
 * - 20초 첫 경유지(주유소) 통과
 * - 37~64초 고속도로: 39초 판교IC 1km 전 안내, 45초 타이어 공기압 경고(휴게소 경유 제안), 판교IC 분기(깜빡이 미리 켬 → 칭찬)
 * - 64~98초 시내: 72초 급제동, 98초 왼쪽 회전(깜빡이 놓침)
 * - 112초 도착, 114초 P단 → 하차 체크
 */
internal object DemoTripScript {
    const val ARRIVE_AT_SECONDS = 112f
    const val PARK_AT_SECONDS = 114f
    private const val FIRST_WAYPOINT_PASSED_AT = 20f
    val ORIGIN = GeoPoint(37.2596, 127.0464)
    val DESTINATION = GeoPoint(37.4019, 127.1086)
    private const val TOTAL_METERS = 17_000
    private const val FUEL_PERCENT = 18

    // 압축 재생이라 거리는 실제 속도보다 빠르게 줄어든다 (합류 1.6km를 37초에).
    private const val MERGE_START_METERS = 1_600f
    private const val MERGE_METERS_PER_SECOND = 43f
    private const val MERGE_ENDS_AT = 37.2f
    private const val EXIT_AT = 64f
    private const val EXIT_METERS_PER_SECOND = 40f
    private const val WINDOW_OPENED_AT = 5f

    private val TURN_MERGE = TurnSpec("demo-merge", TurnDirection.RIGHT, "경부고속도로", isJunction = true)
    private val TURN_EXIT = TurnSpec("demo-exit", TurnDirection.RIGHT, "판교IC", isJunction = true)
    private val TURN_CITY = TurnSpec("demo-city", TurnDirection.LEFT, "판교역로", isJunction = false)

    fun vehicleAt(elapsed: Float?, state: DemoTripState.State): VehicleSnapshot {
        if (elapsed == null) return parkedAtOrigin()
        val parked = elapsed >= PARK_AT_SECONDS
        // 출발 직후 더워서 연 뒷창문을 끝까지 잊는다 (기획 배경의 장면).
        val openWindows = if (elapsed >= WINDOW_OPENED_AT) setOf(CabinArea.REAR_LEFT) - state.closedWindows else emptySet()
        return VehicleSnapshot(
            speedKmh = speedKmh(elapsed),
            gear = if (parked) Gear.PARK else Gear.DRIVE,
            turnSignal = signalAt(elapsed),
            isBrakePressed = elapsed in 71.5f..72.5f || elapsed in 98f..ARRIVE_AT_SECONDS,
            isParkingBrakeOn = parked,
            fuelPercent = FUEL_PERCENT,
            openWindows = openWindows,
            isHeadlightOn = false,
            isWiperOn = false,
            warnings = if (elapsed >= 45f) listOf(VehicleWarning(WarningType.TIRE_PRESSURE_LOW)) else emptyList(),
        )
    }

    fun navigationAt(elapsed: Float?, state: DemoTripState.State): NavigationState {
        if (elapsed == null) return NavigationState()
        val progress = (elapsed / ARRIVE_AT_SECONDS).coerceIn(0f, 1f)
        val turns = upcomingTurns(elapsed)
        return NavigationState(
            isRouteActive = elapsed < ARRIVE_AT_SECONDS,
            nextTurn = turns.firstOrNull(),
            upcomingTurns = turns,
            cautionAhead = cautionAt(elapsed),
            speedLimitKmh = speedLimitAt(elapsed),
            remainingMeters = (TOTAL_METERS * (1 - progress)).roundToInt(),
            remainingSeconds = max(0f, ARRIVE_AT_SECONDS - elapsed).roundToInt() * TIME_COMPRESSION,
            waypointCount = (state.waypointNames.size - passedWaypoints(elapsed)).coerceAtLeast(0),
            isArrived = elapsed >= ARRIVE_AT_SECONDS,
        )
    }

    fun locationAt(elapsed: Float?): GeoPoint {
        val progress = ((elapsed ?: 0f) / ARRIVE_AT_SECONDS).coerceIn(0f, 1f).toDouble()
        return GeoPoint(
            latitude = ORIGIN.latitude + (DESTINATION.latitude - ORIGIN.latitude) * progress,
            longitude = ORIGIN.longitude + (DESTINATION.longitude - ORIGIN.longitude) * progress,
        )
    }

    /** 지나간 경유지 수: 20초에 첫 경유지(주유소)를 지난다. */
    fun passedWaypoints(elapsed: Float): Int = if (elapsed >= FIRST_WAYPOINT_PASSED_AT) 1 else 0

    private fun parkedAtOrigin() = VehicleSnapshot(
        speedKmh = 0f,
        gear = Gear.PARK,
        isParkingBrakeOn = true,
        fuelPercent = FUEL_PERCENT,
        openWindows = emptySet(),
        isHeadlightOn = false,
        isWiperOn = false,
    )

    private fun speedKmh(t: Float): Float = when {
        t < 15f -> lerp(0f, 60f, t / 15f)
        t < 37f -> 60f
        t < 42f -> lerp(60f, 90f, (t - 37f) / 5f)
        t < 64f -> 90f
        t < 68f -> lerp(90f, 45f, (t - 64f) / 4f)
        t < 71.5f -> 45f
        t < 72.5f -> lerp(45f, 25f, t - 71.5f)
        t < 76f -> lerp(25f, 40f, (t - 72.5f) / 3.5f)
        t < 98f -> 40f
        t < ARRIVE_AT_SECONDS -> lerp(40f, 0f, (t - 98f) / (ARRIVE_AT_SECONDS - 98f))
        else -> 0f
    }

    /** 합류는 늦게(35.5초, 7초 알림 뒤) 켜고, 판교IC 분기는 미리(55초) 켠다. 시내 좌회전은 켜지 않는다. */
    private fun signalAt(t: Float): TurnSignal = when (t) {
        in 35.5f..39f, in 55f..63f -> TurnSignal.RIGHT
        else -> TurnSignal.OFF
    }

    private fun mergeDistance(t: Float) = MERGE_START_METERS - MERGE_METERS_PER_SECOND * t

    /** 판교IC는 합류가 끝난 뒤 1km 넘게 남아 있어, 분기 1km 전 안내(약 39초)가 실제로 나온다. */
    private fun exitDistance(t: Float) = (EXIT_AT - t) * EXIT_METERS_PER_SECOND

    private fun upcomingTurns(t: Float): List<TurnGuide> {
        val result = mutableListOf<TurnGuide>()
        if (t < MERGE_ENDS_AT) result += TURN_MERGE.at(mergeDistance(t))
        if (t < EXIT_AT) {
            result += TURN_EXIT.at(
                if (t < MERGE_ENDS_AT) exitDistance(MERGE_ENDS_AT) + (MERGE_ENDS_AT - t) * MERGE_METERS_PER_SECOND else exitDistance(t),
            )
        }
        if (t < 98f) result += TURN_CITY.at(if (t < EXIT_AT) 374f + exitDistance(t) else (98f - t) * 11f)
        return result
    }

    private fun cautionAt(t: Float): CautionPoint? = when {
        t < MERGE_ENDS_AT -> CautionPoint(CautionType.MERGE, mergeDistance(t).roundToInt())
        t in 100f..110f -> CautionPoint(CautionType.SCHOOL_ZONE, ((110f - t) * 8f).roundToInt())
        else -> null
    }

    /** 제한속도는 감속이 끝난 68초에 시내 기준으로 바뀐다. */
    private fun speedLimitAt(t: Float): Int = when {
        t < 37f -> 80
        t < 68f -> 100
        else -> 50
    }

    private fun lerp(from: Float, to: Float, fraction: Float): Float = from + (to - from) * fraction.coerceIn(0f, 1f)

    private data class TurnSpec(val id: String, val direction: TurnDirection, val road: String, val isJunction: Boolean) {
        fun at(distanceMeters: Float) = TurnGuide(id, direction, max(0f, distanceMeters).roundToInt(), road, isJunction)
    }

    /** 120초 데모가 실제 약 40분 주행을 압축한 것이라, 남은 시간은 20배로 보여 준다. */
    private const val TIME_COMPRESSION = 20
}
