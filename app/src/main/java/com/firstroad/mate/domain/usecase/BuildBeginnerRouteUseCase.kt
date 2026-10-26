package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.RoutePlan
import com.firstroad.mate.domain.model.RoutePreference
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.WaypointReason
import com.firstroad.mate.domain.model.WaypointSuggestion
import com.firstroad.mate.domain.repository.PlaceRepository
import com.firstroad.mate.utils.GeoMath
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * ① 초보 맞춤 경로 빌더.
 * 편안한 경로를 기본으로 하고, 연료·주차·휴식 필요에 따라 경유지를 최대 2개 추천한다.
 * 추천 우선순위: 연료/배터리 > 쉬운 주차장 > 휴게소. 최종 순서는 출발지에서 가까운 순.
 */
class BuildBeginnerRouteUseCase @Inject constructor(
    private val placeRepository: PlaceRepository,
) {
    suspend operator fun invoke(destination: Place, origin: GeoPoint, vehicle: VehicleSnapshot): RoutePlan {
        val minutes = estimateMinutes(origin, destination.location)
        val candidates = listOfNotNull(
            energyStop(origin, vehicle),
            easyParking(destination),
            if (minutes >= LONG_DRIVE_MINUTES) restArea(origin, destination) else null,
        )
        val waypoints = candidates
            .take(RoutePlan.MAX_WAYPOINTS)
            .sortedBy { GeoMath.distanceMeters(origin, it.place.location) }
        return RoutePlan(
            destination = destination,
            waypoints = waypoints,
            preference = RoutePreference.COMFORTABLE,
            estimatedMinutes = minutes,
        )
    }

    private suspend fun energyStop(origin: GeoPoint, vehicle: VehicleSnapshot): WaypointSuggestion? {
        val fuel = vehicle.fuelPercent
        val battery = vehicle.evBatteryPercent
        val (category, reason) = when {
            battery != null && battery <= LOW_ENERGY_PERCENT -> PlaceCategory.EV_CHARGER to WaypointReason.LOW_BATTERY
            fuel != null && fuel <= LOW_ENERGY_PERCENT -> PlaceCategory.GAS_STATION to WaypointReason.LOW_FUEL
            else -> return null
        }
        val place = placeRepository.searchNearby(category, origin, ENERGY_SEARCH_RADIUS_M).firstOrNull()
        return place?.let { WaypointSuggestion(it, reason) }
    }

    private suspend fun easyParking(destination: Place): WaypointSuggestion? =
        placeRepository.searchNearby(PlaceCategory.PARKING, destination.location, PARKING_SEARCH_RADIUS_M)
            .firstOrNull()
            ?.let { WaypointSuggestion(it, WaypointReason.EASY_PARKING) }

    private suspend fun restArea(origin: GeoPoint, destination: Place): WaypointSuggestion? {
        val middle = GeoMath.midpoint(origin, destination.location)
        return placeRepository.searchNearby(PlaceCategory.REST_AREA, middle, REST_SEARCH_RADIUS_M)
            .firstOrNull()
            ?.let { WaypointSuggestion(it, WaypointReason.LONG_DRIVE_REST) }
    }

    private fun estimateMinutes(origin: GeoPoint, destination: GeoPoint): Int {
        val roadMeters = GeoMath.distanceMeters(origin, destination) * ROAD_FACTOR
        val metersPerMinute = AVERAGE_SPEED_KMH * 1000.0 / 60.0
        return (roadMeters / metersPerMinute).roundToInt().coerceAtLeast(1)
    }

    private companion object {
        const val LOW_ENERGY_PERCENT = 25
        const val LONG_DRIVE_MINUTES = 60
        const val ENERGY_SEARCH_RADIUS_M = 3_000
        const val PARKING_SEARCH_RADIUS_M = 800
        const val REST_SEARCH_RADIUS_M = 20_000
        const val ROAD_FACTOR = 1.3
        const val AVERAGE_SPEED_KMH = 35.0
    }
}
