package com.firstroad.mate.data.repository

import android.util.Log
import com.firstroad.mate.BuildConfig
import com.firstroad.mate.data.datasource.demo.SamplePlaceDataSource
import com.firstroad.mate.data.network.common.ApiResult
import com.firstroad.mate.data.network.place.PlaceService
import com.firstroad.mate.data.network.place.dto.PlaceDocumentDto
import com.firstroad.mate.data.network.place.dto.PlaceSearchResponseDto
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * 장소 검색. API 키가 있으면 장소 검색 API를, 없거나 실패하면 샘플 장소를 쓴다.
 * DTO → 도메인 모델 변환은 여기서만 한다 (AND-ARC-01).
 */
class PlaceRepositoryImpl @Inject constructor(
    private val placeService: PlaceService,
    private val samplePlaces: SamplePlaceDataSource,
) : PlaceRepository {

    override suspend fun searchNearby(category: PlaceCategory, near: GeoPoint, radiusMeters: Int): List<Place> {
        if (BuildConfig.PLACE_API_KEY.isBlank()) return samplePlaces.searchNearby(category, near, radiusMeters)
        val radius = radiusMeters.coerceIn(0, MAX_RADIUS_METERS)
        val result = when (category) {
            PlaceCategory.PARKING -> placeService.searchByCategory(CODE_PARKING, near.longitude, near.latitude, radius)
            PlaceCategory.GAS_STATION -> placeService.searchByCategory(CODE_GAS, near.longitude, near.latitude, radius)
            PlaceCategory.EV_CHARGER -> placeService.searchByKeyword(KEYWORD_EV, near.longitude, near.latitude, radius)
            PlaceCategory.REST_AREA -> placeService.searchByKeyword(KEYWORD_REST, near.longitude, near.latitude, radius)
            PlaceCategory.REPAIR_SHOP -> placeService.searchByKeyword(KEYWORD_REPAIR, near.longitude, near.latitude, radius)
            PlaceCategory.DESTINATION -> return emptyList()
        }
        return when (result) {
            is ApiResult.Success -> result.body.toPlaces(category)
            is ApiResult.ApiError -> {
                Log.w(TAG, "장소 검색 실패 ${result.code}: 샘플 데이터 사용")
                samplePlaces.searchNearby(category, near, radiusMeters)
            }
            is ApiResult.NetworkError -> samplePlaces.searchNearby(category, near, radiusMeters)
        }
    }

    override suspend fun favoriteDestinations(): List<Place> = samplePlaces.favorites()

    private fun PlaceSearchResponseDto.toPlaces(category: PlaceCategory): List<Place> =
        documents.mapNotNull { it.toPlace(category) }

    private fun PlaceDocumentDto.toPlace(category: PlaceCategory): Place? {
        val lat = latitude.toDoubleOrNull() ?: return null
        val lng = longitude.toDoubleOrNull() ?: return null
        return Place(
            id = id,
            name = placeName,
            address = roadAddressName.ifBlank { addressName },
            location = GeoPoint(lat, lng),
            category = category,
            distanceMeters = distance.toIntOrNull(),
        )
    }

    private companion object {
        const val TAG = "PlaceRepository"
        const val MAX_RADIUS_METERS = 20_000
        const val CODE_PARKING = "PK6"
        const val CODE_GAS = "OL7"
        const val KEYWORD_EV = "전기차 충전소"
        const val KEYWORD_REST = "휴게소"
        const val KEYWORD_REPAIR = "자동차 정비소"
    }
}
