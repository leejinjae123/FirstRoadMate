package com.firstroad.mate.ui.drive.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.ui.common.TurnArrow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.ui.common.ArcGauge
import com.firstroad.mate.ui.common.Caption
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.Numeral
import com.firstroad.mate.ui.common.splitDistance
import com.firstroad.mate.ui.theme.FirstRoad
import kotlin.math.roundToInt

/** 속도 원호: 제한속도를 넘으면 원호가 빨갛게 바뀐다. */
@Composable
fun SpeedWidget(speedKmh: Int?, limitKmh: Int?, modifier: Modifier = Modifier) {
    val speed = speedKmh ?: 0
    val isOver = limitKmh != null && speed > limitKmh
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.drive_speed_caption)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
            ArcGauge(
                value = speed / MAX_GAUGE_KMH,
                modifier = Modifier.size(150.dp),
                color = if (isOver) FirstRoad.tones.action else MaterialTheme.colorScheme.primary,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$speed", style = FirstRoad.type.numeralSmall)
                    Caption(stringResource(R.string.unit_kmh))
                }
            }
            limitKmh?.let { LimitSign(it) }
        }
    }
}

/** 제한속도 표지: 빨간 테두리 원 안의 숫자 (도로 표지와 같은 모양). */
@Composable
private fun LimitSign(limit: Int) {
    ArcGauge(value = 1.5f, modifier = Modifier.size(84.dp), color = FirstRoad.tones.action, trackColor = FirstRoad.tones.action, strokeWidth = 8.dp) {
        Text("$limit", style = MaterialTheme.typography.titleLarge)
    }
}

/** 긴장 지수 원호 + 진정 모드 표시. 60 이상이면 노란색. */
@Composable
fun TensionWidget(tension: Int, isCalmActive: Boolean, modifier: Modifier = Modifier) {
    val color = if (tension >= CALM_THRESHOLD) FirstRoad.tones.prepare else MaterialTheme.colorScheme.primary
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.drive_tension)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
            ArcGauge(value = tension / 100f, modifier = Modifier.size(120.dp), color = color) {
                Text("$tension", style = FirstRoad.type.numeralSmall, color = color)
            }
            if (isCalmActive) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Air, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Text(stringResource(R.string.drive_calm), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** 남은 거리와 도착까지 남은 시간. */
@Composable
fun RemainingWidget(remainingMeters: Int?, remainingMinutes: Int?, destinationName: String, modifier: Modifier = Modifier) {
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.drive_remaining_caption)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.smallGap)) {
            Icon(Icons.Rounded.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column(Modifier.weight(1f)) {
                Text(destinationName, style = MaterialTheme.typography.titleMedium)
                remainingMinutes?.let { Text(stringResource(R.string.drive_eta_nav, it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            remainingMeters?.let {
                val (value, unit) = splitDistance(it)
                Numeral(value, unit)
            }
        }
    }
}

/**
 * ③ 리허설: 다음 회전 뒤에 이어지는 갈림길 두 개를 순서대로 미리 보여 준다 ("5.3 km · 약 4분 · 판교IC · 갈림길").
 * 예상 시간은 지금 속도로 계산한다 (멈춰 있으면 생략).
 */
@Composable
fun UpcomingTurnsCard(turns: List<TurnGuide>, speedKmh: Int?, modifier: Modifier = Modifier) {
    if (turns.isEmpty()) return
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.drive_upcoming_caption)) {
        turns.forEach { turn ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
                TurnArrow(turn.direction, Modifier.size(44.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                val (value, unit) = splitDistance(turn.distanceMeters)
                Text("$value $unit", style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(140.dp))
                val minutes = speedKmh?.takeIf { it >= MIN_SPEED_FOR_ETA }?.let { speed ->
                    ((turn.distanceMeters / 1000f) / speed * MINUTES_PER_HOUR).roundToInt().coerceAtLeast(1)
                }
                Text(
                    minutes?.let { stringResource(R.string.drive_eta, it) }.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(110.dp),
                )
                Text(turn.roadName.orEmpty(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (turn.isHighwayJunction) {
                    Caption(stringResource(R.string.drive_junction), color = FirstRoad.tones.prepare)
                }
            }
        }
    }
}

private const val MAX_GAUGE_KMH = 140f
private const val MIN_SPEED_FOR_ETA = 10
private const val MINUTES_PER_HOUR = 60f
private const val CALM_THRESHOLD = 60
