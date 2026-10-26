package com.firstroad.mate.ui.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.IconDisc
import com.firstroad.mate.ui.common.InfoRow
import com.firstroad.mate.ui.common.LevelBar
import com.firstroad.mate.ui.common.RingProgress
import com.firstroad.mate.ui.common.favoriteIcon
import com.firstroad.mate.ui.theme.FirstRoad

/** 자주 가는 곳 타일: 아이콘 원 + 이름. 고르면 시안 테두리와 옅은 시안 바탕. */
@Composable
fun DestinationTile(place: Place, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dimens = FirstRoad.dimens
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = dimens.minTouch).widthIn(min = 200.dp),
        shape = RoundedCornerShape(dimens.cornerRadius),
        color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 2.dp else dimens.hairline, if (isSelected) accent else MaterialTheme.colorScheme.outline),
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconDisc(place.favoriteIcon(), if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(place.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** '내 차' 위젯: 연료(또는 배터리) 막대와 지난 주차 위치. */
@Composable
fun CarWidget(
    fuelPercent: Int?,
    batteryPercent: Int?,
    lastParking: String?,
    modifier: Modifier = Modifier,
) {
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.home_widget_car)) {
        val isEv = fuelPercent == null && batteryPercent != null
        val percent = if (isEv) batteryPercent else fuelPercent
        InfoRow(
            icon = if (isEv) Icons.Rounded.BatteryChargingFull else Icons.Rounded.LocalGasStation,
            label = stringResource(if (isEv) R.string.home_battery else R.string.home_fuel),
            value = percent?.let { "$it%" } ?: "–",
            valueColor = if ((percent ?: 100) < LOW_ENERGY_PERCENT) FirstRoad.tones.prepare else MaterialTheme.colorScheme.onSurface,
        )
        percent?.let {
            LevelBar(it / 100f, if (it < LOW_ENERGY_PERCENT) FirstRoad.tones.prepare else MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth())
        }
        InfoRow(
            icon = Icons.Rounded.LocalParking,
            label = stringResource(R.string.home_parking_last),
            value = lastParking ?: stringResource(R.string.home_parking_none),
        )
    }
}

/** '초보 졸업까지' 위젯: 레벨 고리 + 다음 레벨까지 남은 주행. */
@Composable
fun LevelWidget(level: Int, progress: Float, drivesToNextLevel: Int?, modifier: Modifier = Modifier) {
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.home_widget_level)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
            RingProgress(progress = if (drivesToNextLevel == null) 1f else progress, modifier = Modifier.size(104.dp), strokeWidth = 10.dp) {
                Text(stringResource(R.string.home_level_short, level), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.home_level, level), style = MaterialTheme.typography.titleLarge)
                Text(
                    drivesToNextLevel?.let { stringResource(R.string.home_next_level, level + 1, it) } ?: stringResource(R.string.home_graduated),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val LOW_ENERGY_PERCENT = 25
