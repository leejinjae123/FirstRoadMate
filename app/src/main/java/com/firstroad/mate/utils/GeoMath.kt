package com.firstroad.mate.utils

import com.firstroad.mate.domain.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** 위경도 거리 계산 (순수 함수). */
object GeoMath {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun distanceMeters(from: GeoPoint, to: GeoPoint): Double {
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun midpoint(a: GeoPoint, b: GeoPoint): GeoPoint =
        GeoPoint((a.latitude + b.latitude) / 2, (a.longitude + b.longitude) / 2)

    /** 점에서 선분(start-end)까지의 대략적인 거리. 도시 규모에서는 평면 근사로 충분하다. */
    fun distanceToSegmentMeters(point: GeoPoint, start: GeoPoint, end: GeoPoint): Double {
        val metersPerDegLat = 111_320.0
        val metersPerDegLng = 111_320.0 * cos(Math.toRadians(start.latitude))
        val px = (point.longitude - start.longitude) * metersPerDegLng
        val py = (point.latitude - start.latitude) * metersPerDegLat
        val ex = (end.longitude - start.longitude) * metersPerDegLng
        val ey = (end.latitude - start.latitude) * metersPerDegLat
        val lengthSquared = ex * ex + ey * ey
        if (lengthSquared == 0.0) return sqrt(px * px + py * py)
        val t = ((px * ex + py * ey) / lengthSquared).coerceIn(0.0, 1.0)
        val dx = px - t * ex
        val dy = py - t * ey
        return sqrt(dx * dx + dy * dy)
    }
}
