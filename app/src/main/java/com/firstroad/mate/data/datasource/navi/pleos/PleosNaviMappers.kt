package com.firstroad.mate.data.datasource.navi.pleos

import ai.pleos.playground.navi.constants.CautionPointType
import ai.pleos.playground.navi.constants.RouteOption
import ai.pleos.playground.navi.constants.TurnType
import ai.pleos.playground.navi.constants.WaypointIndex
import ai.pleos.playground.navi.data.CautionPoint as SdkCautionPoint
import ai.pleos.playground.navi.data.RequestWaypointInfo
import ai.pleos.playground.navi.data.RouteInfo
import ai.pleos.playground.navi.data.TBTInfo
import com.firstroad.mate.domain.model.CautionPoint
import com.firstroad.mate.domain.model.CautionType
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePreference
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import kotlin.math.roundToInt

// NaviHelper SDK 타입 ↔ 도메인 모델 변환.

private const val DEFAULT_POI_SUB_ID = "0"

internal fun TBTInfo.toTurnGuide(): TurnGuide {
    val name = type.name
    val direction = when {
        type == TurnType.TURN_UTURN -> TurnDirection.U_TURN
        name == "TURN_RIGHT" || name.endsWith("_R") -> TurnDirection.RIGHT
        name == "TURN_LEFT" || name.endsWith("_L") -> TurnDirection.LEFT
        else -> TurnDirection.STRAIGHT
    }
    val isJunction = "_IC_" in name ||
        type == TurnType.TURN_DEVIDE_MERGE ||
        type == TurnType.TURN_ENTER_SERVICEAREA ||
        type == TurnType.TURN_RESTAREA
    return TurnGuide(
        id = "$name:$description",
        direction = direction,
        distanceMeters = distance.roundToInt(),
        roadName = description.ifBlank { null },
        isHighwayJunction = isJunction,
    )
}

internal fun SdkCautionPoint.toDomain(): CautionPoint = CautionPoint(
    type = when (type) {
        CautionPointType.SCHOOL_ZONE -> CautionType.SCHOOL_ZONE
        CautionPointType.ACCIDENT_PRONE_AREA -> CautionType.ACCIDENT_ZONE
        CautionPointType.DOWNHILL_CAUTION_AREA,
        CautionPointType.SLIPPERY_ROAD_ALERT,
        CautionPointType.NARROW_ROAD -> CautionType.SHARP_CURVE
        else -> CautionType.OTHER
    },
    distanceMeters = remainDistance,
)

internal fun RoutePreference.toSdk(): RouteOption = when (this) {
    RoutePreference.COMFORTABLE -> RouteOption.COMFORTABLE
    RoutePreference.RECOMMENDED -> RouteOption.RECOMMENDED
    RoutePreference.SHORTEST_TIME -> RouteOption.SHORT_TIME
    RoutePreference.FREE_ROAD -> RouteOption.FREE
}

internal fun Place.toRouteInfo(preference: RoutePreference): RouteInfo = RouteInfo(
    latitude = location.latitude,
    longitude = location.longitude,
    poiName = name,
    poiId = id,
    address = address,
    poiSubId = DEFAULT_POI_SUB_ID,
    routeOption = preference.toSdk(),
)

internal fun Place.toWaypointInfo(slot: Int): RequestWaypointInfo = RequestWaypointInfo(
    latitude = location.latitude,
    longitude = location.longitude,
    poiName = name,
    waypointIndex = if (slot == 0) WaypointIndex.FIRST else WaypointIndex.SECOND,
    poiId = id,
    address = address,
    poiSubId = DEFAULT_POI_SUB_ID,
)
