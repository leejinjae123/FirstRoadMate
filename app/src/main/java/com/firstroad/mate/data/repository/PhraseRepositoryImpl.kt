package com.firstroad.mate.data.repository

import android.content.Context
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.LevelTier
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.VoiceScreen
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WarningType
import com.firstroad.mate.domain.repository.PhraseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

/** Guidance → strings.xml 문장. 화면 표시와 음성 안내가 같은 문장을 쓴다. */
class PhraseRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PhraseRepository {

    override fun phrase(guidance: Guidance): String = when (guidance) {
        is Guidance.PrepareLane -> if (guidance.isHighway) {
            text(R.string.g_prepare_lane_highway, distance(guidance.distanceMeters), direction(guidance.direction))
        } else {
            text(R.string.g_prepare_lane, distance(guidance.distanceMeters), direction(guidance.direction))
        }
        is Guidance.SignalReminder -> text(R.string.g_signal_reminder, direction(guidance.direction))
        Guidance.SignalPraise -> text(R.string.g_signal_praise)
        Guidance.HardBrakeTip -> text(R.string.g_hard_brake)
        is Guidance.SpeedOver -> text(R.string.g_speed_over, guidance.limitKmh)
        is Guidance.RouteBriefing -> briefing(guidance)
        is Guidance.NextTurnAnswer -> text(R.string.g_next_turn, distance(guidance.distanceMeters), direction(guidance.direction))
        Guidance.NoActiveRoute -> text(R.string.g_no_route)
        is Guidance.WarningNotice -> text(R.string.g_warning, warning(guidance.type), levelAdvice(guidance.level))
        is Guidance.ContinueAnswer -> guidance.level?.let(::levelAdvice) ?: text(R.string.g_continue_none)
        is Guidance.CalmModeOn -> text(if (guidance.cabinAdjusted) R.string.g_calm_adjusted else R.string.g_calm_hint)
        Guidance.PassedWell -> text(R.string.g_passed_well)
        is Guidance.RouteReady -> if (guidance.stops.isEmpty()) {
            text(R.string.g_route_ready, guidance.destinationName, guidance.minutes)
        } else {
            text(R.string.g_route_ready_stops, guidance.destinationName, guidance.minutes, guidance.stops.joinToString(", ") { category(it) })
        }
        is Guidance.AskAddWaypoint -> text(R.string.g_ask_waypoint, categoryObject(guidance.category))
        is Guidance.WaypointAdded -> text(R.string.g_waypoint_added, guidance.placeName)
        Guidance.WaypointRemoved -> text(R.string.g_waypoint_removed)
        Guidance.WaypointFull -> text(R.string.g_waypoint_full)
        is Guidance.NoPlaceFound -> text(R.string.g_no_place, categoryObject(guidance.category))
        is Guidance.CheckIssue -> checkIssue(guidance)
        Guidance.AllClear -> text(R.string.g_all_clear)
        is Guidance.WindowClosed -> text(R.string.g_window_closed, area(guidance.area))
        Guidance.NoOpenWindow -> text(R.string.g_no_open_window)
        Guidance.ParkFirst -> text(R.string.g_park_first)
        Guidance.ControlNotPermitted -> text(R.string.g_control_not_permitted)
        is Guidance.ParkingSaved -> guidance.memo
            ?.let { text(R.string.g_parking_saved_memo, it) }
            ?: text(R.string.g_parking_saved)
        is Guidance.CarLocation -> carLocation(guidance)
        Guidance.NoParkingRecord -> text(R.string.g_no_parking)
        is Guidance.LevelUp -> if (guidance.level >= LevelTier.GRADUATE.level) {
            text(R.string.g_graduated)
        } else {
            text(R.string.g_level_up, guidance.level)
        }
        is Guidance.Help -> text(
            when (guidance.screen) {
                VoiceScreen.HOME -> R.string.g_help_home
                VoiceScreen.DRIVE -> R.string.g_help_drive
                VoiceScreen.PARK -> R.string.g_help_park
            },
        )
        Guidance.SpotMarked -> text(R.string.g_spot_marked)
        Guidance.NothingToRepeat -> text(R.string.g_nothing_repeat)
        Guidance.NotUnderstood -> text(R.string.g_not_understood)
        Guidance.Dismissed -> text(R.string.g_dismissed)
        Guidance.Arrived -> text(R.string.g_arrived)
    }

    /**
     * "어려운 구간 3곳이에요. 첫 갈림길 1.6km 앞 오른쪽." 두 문장 안에 끝낸다.
     * 문장 사이 공백은 코드에서 붙인다 (strings.xml 앞뒤 공백은 리소스 컴파일 때 지워진다).
     */
    private fun briefing(guidance: Guidance.RouteBriefing): String {
        if (guidance.difficultCount == 0) return text(R.string.g_briefing_none)
        val count = if (guidance.bookmarkedCount > 0) {
            text(R.string.g_briefing_bookmark, guidance.difficultCount)
        } else {
            text(R.string.g_briefing, guidance.difficultCount)
        }
        val first = guidance.firstJunction?.let { text(R.string.g_briefing_first, distance(it.distanceMeters), direction(it.direction)) }
        return listOfNotNull(count, first).joinToString(" ")
    }

    private fun checkIssue(guidance: Guidance.CheckIssue): String = when (guidance.type) {
        CheckType.WINDOW -> text(R.string.g_check_window, guidance.area?.let(::area).orEmpty())
        CheckType.HEADLIGHT -> text(R.string.g_check_headlight)
        CheckType.PARKING_BRAKE -> text(R.string.g_check_parking_brake)
    }

    /** 950m 이상은 km로 말한다 (998m → "1km", 1,600m → "1.6km"). */
    private fun distance(meters: Int): String = if (meters < KM_THRESHOLD_METERS) {
        text(R.string.unit_meters, (meters / ROUND_METERS) * ROUND_METERS)
    } else {
        val km = String.format(Locale.KOREA, "%.1f", meters / METERS_PER_KM.toFloat()).trimEnd('0').trimEnd('.')
        text(R.string.unit_kilometers, km)
    }

    /** 1분 미만은 "방금", 60분 미만은 분, 이상은 시간으로 ("540분" 대신 "9시간"). */
    private fun carLocation(guidance: Guidance.CarLocation): String {
        val memo = guidance.memo
        return when {
            guidance.minutesAgo < 1 && memo != null -> text(R.string.g_car_location_now_memo, memo)
            guidance.minutesAgo < 1 -> text(R.string.g_car_location_now)
            memo != null -> text(R.string.g_car_location_memo, elapsed(guidance.minutesAgo), memo)
            else -> text(R.string.g_car_location, elapsed(guidance.minutesAgo))
        }
    }

    private fun elapsed(minutes: Long): String = if (minutes < MINUTES_PER_HOUR) {
        text(R.string.unit_minutes, minutes.toInt())
    } else {
        text(R.string.unit_hours, (minutes / MINUTES_PER_HOUR).toInt())
    }

    private fun direction(direction: TurnDirection): String = text(
        when (direction) {
            TurnDirection.LEFT -> R.string.dir_left
            TurnDirection.RIGHT -> R.string.dir_right
            TurnDirection.U_TURN -> R.string.dir_uturn
            TurnDirection.STRAIGHT -> R.string.dir_straight
        },
    )

    private fun area(area: CabinArea): String = text(
        when (area) {
            CabinArea.FRONT_LEFT -> R.string.area_front_left
            CabinArea.FRONT_RIGHT -> R.string.area_front_right
            CabinArea.REAR_LEFT -> R.string.area_rear_left
            CabinArea.REAR_RIGHT -> R.string.area_rear_right
        },
    )

    private fun category(category: PlaceCategory): String = text(
        when (category) {
            PlaceCategory.GAS_STATION -> R.string.cat_gas
            PlaceCategory.EV_CHARGER -> R.string.cat_ev
            PlaceCategory.PARKING -> R.string.cat_parking
            PlaceCategory.REST_AREA -> R.string.cat_rest
            PlaceCategory.REPAIR_SHOP -> R.string.cat_repair
            PlaceCategory.DESTINATION -> R.string.cat_destination
        },
    )

    private fun categoryObject(category: PlaceCategory): String = text(
        when (category) {
            PlaceCategory.GAS_STATION -> R.string.cat_gas_obj
            PlaceCategory.EV_CHARGER -> R.string.cat_ev_obj
            PlaceCategory.PARKING -> R.string.cat_parking_obj
            PlaceCategory.REST_AREA -> R.string.cat_rest_obj
            PlaceCategory.REPAIR_SHOP -> R.string.cat_repair_obj
            PlaceCategory.DESTINATION -> R.string.cat_destination_obj
        },
    )

    private fun warning(type: WarningType): String = text(
        when (type) {
            WarningType.TIRE_PRESSURE_LOW -> R.string.warn_tire
            WarningType.LOW_FUEL -> R.string.warn_low_fuel
            WarningType.LOW_BATTERY -> R.string.warn_low_battery
            WarningType.ENGINE_CHECK -> R.string.warn_engine_check
            WarningType.ENGINE_OVERHEAT -> R.string.warn_overheat
            WarningType.BRAKE_SYSTEM -> R.string.warn_brake
            WarningType.OIL_PRESSURE -> R.string.warn_oil
            WarningType.AIRBAG -> R.string.warn_airbag
            WarningType.UNKNOWN -> R.string.warn_unknown
        },
    )

    private fun levelAdvice(level: WarningLevel): String = text(
        when (level) {
            WarningLevel.STOP_NOW -> R.string.level_stop
            WarningLevel.CHECK_TODAY -> R.string.level_today
            WarningLevel.INFO -> R.string.level_info
        },
    )

    private fun text(id: Int, vararg args: Any): String = context.getString(id, *args)

    private companion object {
        const val METERS_PER_KM = 1_000
        const val KM_THRESHOLD_METERS = 950
        const val ROUND_METERS = 10
        const val MINUTES_PER_HOUR = 60L
    }
}
