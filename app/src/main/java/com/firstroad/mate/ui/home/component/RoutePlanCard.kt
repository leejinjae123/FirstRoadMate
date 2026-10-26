package com.firstroad.mate.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.RoutePlan
import com.firstroad.mate.domain.model.WaypointReason
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 와이어프레임 A의 추천 경로 카드: 집 ─▶ ① 경유지 ─▶ 목적지, 그리고 "왜?"
 * compact: 낮은 화면에서는 글자를 줄이고 시간과 이유를 한 줄로 합친다.
 */
@Composable
fun RoutePlanCard(
    plan: RoutePlan,
    fuelPercent: Int?,
    batteryPercent: Int?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val dimens = FirstRoad.dimens
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
            if (!compact) {
                Text(stringResource(R.string.home_recommended), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            val stops = buildList {
                add(stringResource(R.string.home_origin))
                plan.waypoints.forEachIndexed { index, waypoint -> add("${CIRCLED_NUMBERS[index]} ${waypoint.place.name}") }
                add(plan.destination.name)
            }
            Text(
                stops.joinToString(" ─▶ "),
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            )
            val minutes = stringResource(R.string.home_minutes, plan.estimatedMinutes)
            val reasons = plan.waypoints.map { reasonText(it.reason, fuelPercent, batteryPercent) }
            val why = if (reasons.isEmpty()) null else stringResource(R.string.home_why) + "  " + reasons.joinToString(" · ")
            if (compact) {
                Text(listOfNotNull(minutes, why).joinToString("  ·  "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(minutes, style = MaterialTheme.typography.bodyLarge)
                why?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
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

private val CIRCLED_NUMBERS = listOf("①", "②")
