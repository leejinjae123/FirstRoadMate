package com.firstroad.mate.ui.navigation

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Place

/**
 * 화면 경로와 인자. 화면 사이에는 객체 대신 이름·좌표 같은 값만 넘긴다.
 * 인자는 쿼리(기본값 "")로 넘겨 빈 이름에서도 경로 매칭이 깨지지 않게 한다.
 */
object AppDestinations {
    const val HOME = "home"
    const val ARG_NAME = "name"
    const val ARG_LAT = "lat"
    const val ARG_LNG = "lng"
    private const val QUERY = "?$ARG_NAME={$ARG_NAME}&$ARG_LAT={$ARG_LAT}&$ARG_LNG={$ARG_LNG}"
    const val DRIVE = "drive$QUERY"
    const val PARK = "park$QUERY"

    val tripArguments: List<NamedNavArgument> = listOf(ARG_NAME, ARG_LAT, ARG_LNG).map { name ->
        navArgument(name) {
            type = NavType.StringType
            defaultValue = ""
        }
    }

    fun drive(place: Place): String = "drive" + query(place.name, place.location)

    fun park(name: String, point: GeoPoint?): String = "park" + query(name, point)

    fun parsePoint(lat: String?, lng: String?): GeoPoint? {
        val latitude = lat?.toDoubleOrNull() ?: return null
        val longitude = lng?.toDoubleOrNull() ?: return null
        return GeoPoint(latitude, longitude)
    }

    private fun query(name: String, point: GeoPoint?): String =
        "?$ARG_NAME=${Uri.encode(name)}&$ARG_LAT=${point?.latitude ?: ""}&$ARG_LNG=${point?.longitude ?: ""}"
}
