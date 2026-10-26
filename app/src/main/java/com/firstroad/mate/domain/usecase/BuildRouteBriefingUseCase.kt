package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.DifficultSpot
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.utils.GeoMath
import javax.inject.Inject

/**
 * 출발 전 "어려운 구간" 브리핑 (③ 고속도로 리허설 포함). 내비의 주의 구간·고속도로 분기와,
 * 운전자가 "여기 어려웠어"라고 표시한 지점 중 경로 근처에 있는 것을 센다.
 * 경로 선형을 받을 수 없으므로 출발지-목적지 직선에서 1km 이내를 '경로 근처'로 본다.
 */
class BuildRouteBriefingUseCase @Inject constructor() {

    operator fun invoke(
        navigation: NavigationState,
        spots: List<DifficultSpot>,
        origin: GeoPoint?,
        destination: GeoPoint,
    ): Guidance.RouteBriefing {
        val junctions = navigation.upcomingTurns.filter { it.isHighwayJunction }
        val cautions = if (navigation.cautionAhead != null) 1 else 0
        val bookmarked = if (origin == null) 0 else spots.count {
            GeoMath.distanceToSegmentMeters(it.location, origin, destination) <= NEAR_ROUTE_METERS
        }
        return Guidance.RouteBriefing(
            difficultCount = junctions.size + cautions + bookmarked,
            bookmarkedCount = bookmarked,
            highwayJunctions = junctions.size,
            firstJunction = junctions.minByOrNull { it.distanceMeters },
        )
    }

    private companion object {
        const val NEAR_ROUTE_METERS = 1_000.0
    }
}
