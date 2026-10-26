package com.firstroad.mate.ui.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.RoutePlan
import com.firstroad.mate.domain.model.WaypointReason
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.IconDisc
import com.firstroad.mate.ui.common.Numeral
import com.firstroad.mate.ui.common.favoriteIcon
import com.firstroad.mate.ui.common.icon
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 와이어프레임 A의 추천 경로 카드: 왼쪽에 큰 숫자(약 37분), 오른쪽에 지금 위치 → ① 경유지 → 목적지,
 * 아래에 "왜?" 이유 칩. compact: 낮은 화면에서 숫자와 아이콘을 줄인다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutePlanCard(
    plan: RoutePlan,
    fuelPercent: Int?,
    batteryPercent: Int?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    /** 낮은 화면에서 '경유지 빼기'를 카드 안 작은 버튼으로 둔다 (null이면 숨김). */
    onRemoveWaypoints: (() -> Unit)? = null,
) {
    val dimens = FirstRoad.dimens
    CockpitCard(
        modifier = modifier.fillMaxWidth(),
        caption = if (compact) null else stringResource(R.string.home_recommended),
        contentPadding = if (compact) 18.dp else dimens.cardPadding,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            Numeral(plan.estimatedMinutes.toString(), stringResource(R.string.home_route_minutes), huge = !compact, color = MaterialTheme.colorScheme.primary)
            Box(Modifier.width(1.dp).height(if (compact) 56.dp else 88.dp).background(MaterialTheme.colorScheme.outline))
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                val iconSize = if (compact) 36.dp else 44.dp
                // 낮은 화면에서는 '지금 위치'를 빼서 경로가 한 줄에 들어오게 한다.
                if (!compact) Stop(Icons.Rounded.MyLocation, stringResource(R.string.home_origin), MaterialTheme.colorScheme.onSurfaceVariant, iconSize)
                plan.waypoints.forEachIndexed { index, waypoint ->
                    if (!compact || index > 0) Connector()
                    Stop(waypoint.place.category.icon(), waypoint.place.name, FirstRoad.tones.prepare, iconSize)
                }
                Connector()
                Stop(plan.destination.favoriteIcon(), plan.destination.name, MaterialTheme.colorScheme.primary, iconSize)
            }
        }
        val reasons = plan.waypoints.map { reasonText(it.reason, fuelPercent, batteryPercent) }
        if (reasons.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.home_why), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                reasons.forEach { ReasonChip(it) }
                onRemoveWaypoints?.let {
                    Box(Modifier.weight(1f))
                    OutlinedButton(onClick = it, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(24.dp))
                        Text("  " + stringResource(R.string.home_remove_waypoints), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun Stop(icon: ImageVector, name: String, tint: Color, iconSize: androidx.compose.ui.unit.Dp) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconDisc(icon, tint, size = iconSize)
        Text(name, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Connector() {
    Box(Modifier.width(28.dp).height(2.dp).background(MaterialTheme.colorScheme.outlineVariant))
}

@Composable
private fun ReasonChip(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = FirstRoad.tones.prepare,
        modifier = Modifier
            .background(FirstRoad.tones.prepare.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 4.dp),
    )
}

@Composable
private fun reasonText(reason: WaypointReason, fuelPercent: Int?, batteryPercent: Int?): String = when (reason) {
    WaypointReason.LOW_FUEL -> fuelPercent?.let { stringResource(R.string.reason_low_fuel, it) }
        ?: stringResource(R.string.reason_low_energy)
    WaypointReason.LOW_BATTERY -> batteryPercent?.let { stringResource(R.string.reason_low_battery, it) }
        ?: stringResource(R.string.reason_low_energy)
    WaypointReason.EASY_PARKING -> stringResource(R.string.reason_easy_parking)
    WaypointReason.LONG_DRIVE_REST -> stringResource(R.string.reason_rest)
    WaypointReason.REPAIR_NEEDED -> stringResource(R.string.reason_repair)
    WaypointReason.REQUESTED_BY_VOICE -> stringResource(R.string.reason_voice)
}
