package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.RoutePreference
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.WaypointReason
import com.firstroad.mate.domain.repository.PlaceRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildBeginnerRouteUseCaseTest {

    private val origin = GeoPoint(37.2596, 127.0464)
    private val destination = Place("d", "판교 회사", "", GeoPoint(37.4019, 127.1086), PlaceCategory.DESTINATION)

    private val places = object : PlaceRepository {
        override suspend fun searchNearby(category: PlaceCategory, near: GeoPoint, radiusMeters: Int): List<Place> =
            listOf(Place(category.name, category.name, "", near, category))

        override suspend fun favoriteDestinations(): List<Place> = emptyList()
    }

    private val useCase = BuildBeginnerRouteUseCase(places)

    @Test
    fun `연료가 부족하면 주유소와 주차장을 경유지로 추천하고 편안한 경로를 쓴다`() = runTest {
        val plan = useCase(destination, origin, VehicleSnapshot(fuelPercent = 18))
        assertEquals(RoutePreference.COMFORTABLE, plan.preference)
        assertEquals(listOf(WaypointReason.LOW_FUEL, WaypointReason.EASY_PARKING), plan.waypoints.map { it.reason })
    }

    @Test
    fun `연료가 충분하면 주차장만 추천한다`() = runTest {
        val plan = useCase(destination, origin, VehicleSnapshot(fuelPercent = 70))
        assertEquals(listOf(WaypointReason.EASY_PARKING), plan.waypoints.map { it.reason })
    }

    @Test
    fun `경유지는 2개를 넘지 않는다`() = runTest {
        val far = destination.copy(location = GeoPoint(35.1796, 129.0756))
        val plan = useCase(far, origin, VehicleSnapshot(fuelPercent = 10))
        assertEquals(2, plan.waypoints.size)
    }
}
