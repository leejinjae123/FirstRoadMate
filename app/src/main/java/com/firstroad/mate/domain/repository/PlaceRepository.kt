package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory

interface PlaceRepository {
    /** 기준 위치 근처의 장소를 가까운 순으로 찾는다. 실패하면 빈 목록. */
    suspend fun searchNearby(category: PlaceCategory, near: GeoPoint, radiusMeters: Int): List<Place>

    /** 홈 화면의 자주 가는 목적지. */
    suspend fun favoriteDestinations(): List<Place>
}
