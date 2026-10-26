package com.firstroad.mate.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.EvStation
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.PlaceCategory

/** 장소 종류 → 아이콘. 경유지·경고 카드·하차 화면이 같은 아이콘을 쓴다. */
fun PlaceCategory.icon(): ImageVector = when (this) {
    PlaceCategory.GAS_STATION -> Icons.Rounded.LocalGasStation
    PlaceCategory.EV_CHARGER -> Icons.Rounded.EvStation
    PlaceCategory.PARKING -> Icons.Rounded.LocalParking
    PlaceCategory.REST_AREA -> Icons.Rounded.LocalCafe
    PlaceCategory.REPAIR_SHOP -> Icons.Rounded.Build
    PlaceCategory.DESTINATION -> Icons.Rounded.Flag
}

/** 자주 가는 곳 이름으로 아이콘을 고른다 (회사·집·마트). */
fun Place.favoriteIcon(): ImageVector = when {
    name.containsAny("회사", "사무실", "직장") -> Icons.Rounded.Work
    name.containsAny("집", "댁", "본가") -> Icons.Rounded.Home
    name.containsAny("마트", "시장", "쇼핑") -> Icons.Rounded.ShoppingCart
    category != PlaceCategory.DESTINATION -> category.icon()
    else -> Icons.Rounded.Place
}

private fun String.containsAny(vararg words: String) = words.any { contains(it) }
