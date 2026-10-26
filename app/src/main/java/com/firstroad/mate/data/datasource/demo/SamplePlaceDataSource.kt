package com.firstroad.mate.data.datasource.demo

import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.utils.GeoMath
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * 장소 검색 API 키가 없거나 호출이 실패할 때 쓰는 샘플 장소 (데모 경로: 수원 영통 → 판교).
 * 좌표와 이름은 시연용 예시다.
 */
@Singleton
class SamplePlaceDataSource @Inject constructor() {

    fun searchNearby(category: PlaceCategory, near: GeoPoint, radiusMeters: Int): List<Place> =
        SAMPLES.filter { it.category == category }
            .map { it.copy(distanceMeters = GeoMath.distanceMeters(near, it.location).roundToInt()) }
            .filter { (it.distanceMeters ?: Int.MAX_VALUE) <= radiusMeters * RADIUS_TOLERANCE }
            .sortedBy { it.distanceMeters }

    fun favorites(): List<Place> = FAVORITES

    private companion object {
        /** 샘플이 적어서 반경을 조금 넓게 본다. */
        const val RADIUS_TOLERANCE = 3

        // 블라인드 심사: 실존 기업·개인을 떠올리게 하는 이름·번지를 쓰지 않는다 (모두 가상의 예시).
        val FAVORITES = listOf(
            Place("fav-office", "판교 회사", "경기 성남시 분당구 (예시)", DemoTripScript.DESTINATION, PlaceCategory.DESTINATION),
            Place("fav-parents", "부모님 댁", "경기 용인시 수지구 (예시)", GeoPoint(37.3236, 127.0958), PlaceCategory.DESTINATION),
            Place("fav-mart", "자주 가는 마트", "경기 수원시 영통구 (예시)", GeoPoint(37.2510, 127.0710), PlaceCategory.DESTINATION),
        )

        val SAMPLES = listOf(
            Place("gas-1", "영통 셀프주유소", "경기 수원시 영통구 (예시)", GeoPoint(37.2640, 127.0500), PlaceCategory.GAS_STATION),
            Place("gas-2", "신갈 주유소", "경기 용인시 기흥구 (예시)", GeoPoint(37.2840, 127.1060), PlaceCategory.GAS_STATION),
            Place("ev-1", "영통 전기차 충전소", "경기 수원시 영통구 (예시)", GeoPoint(37.2620, 127.0520), PlaceCategory.EV_CHARGER),
            Place("park-1", "판교 공영주차장", "경기 성남시 분당구 (예시)", GeoPoint(37.4005, 127.1101), PlaceCategory.PARKING),
            Place("park-2", "판교역 환승주차장", "경기 성남시 분당구 (예시)", GeoPoint(37.3947, 127.1112), PlaceCategory.PARKING),
            Place("rest-1", "기흥 휴게소", "경부고속도로 서울 방향 (예시)", GeoPoint(37.2560, 127.1150), PlaceCategory.REST_AREA),
            Place("rest-2", "죽전 휴게소", "경부고속도로 서울 방향 (예시)", GeoPoint(37.3350, 127.1050), PlaceCategory.REST_AREA),
            Place("repair-1", "판교 자동차 정비소", "경기 성남시 분당구 (예시)", GeoPoint(37.3990, 127.1000), PlaceCategory.REPAIR_SHOP),
            Place("repair-2", "영통 타이어 정비소", "경기 수원시 영통구 (예시)", GeoPoint(37.2550, 127.0600), PlaceCategory.REPAIR_SHOP),
        )
    }
}
