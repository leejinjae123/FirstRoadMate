package com.firstroad.mate.ui.park.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material.icons.rounded.Window
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.ParkingCheckItem
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.IconDisc
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 와이어프레임 D 윗부분: 창문·전조등·주차 브레이크를 같은 크기의 타일 3장으로.
 * 문제가 있는 타일만 노란 테두리와 '닫기' 버튼이 생긴다.
 */
@Composable
fun CheckList(
    checks: List<ParkingCheckItem>,
    onFix: (ParkingCheckItem) -> Unit,
    modifier: Modifier = Modifier,
    showFixButtons: Boolean = true,
) {
    val dimens = FirstRoad.dimens
    Column(modifier, verticalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
            checks.forEach { item -> CheckTile(item, onFix, Modifier.weight(1f), showFixButtons) }
        }
        if (checks.size < CheckType.entries.size) {
            Text(stringResource(R.string.park_skipped), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CheckTile(item: ParkingCheckItem, onFix: (ParkingCheckItem) -> Unit, modifier: Modifier = Modifier, showFixButton: Boolean = true) {
    val color = if (item.isOk) FirstRoad.tones.ok else FirstRoad.tones.prepare
    CockpitCard(modifier = modifier, accent = if (item.isOk) null else color, contentPadding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconDisc(item.type.icon(), color, size = 52.dp)
            Column(Modifier.weight(1f)) {
                Text(label(item), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.isOk) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.height(22.dp))
                    Text(status(item), style = MaterialTheme.typography.labelMedium, color = color)
                }
            }
        }
        if (showFixButton && !item.isOk && item.canFixRemotely) {
            BigActionButton(stringResource(R.string.park_fix_window), { onFix(item) }, Modifier.fillMaxWidth())
        }
    }
}

private fun CheckType.icon(): ImageVector = when (this) {
    CheckType.WINDOW -> Icons.Rounded.Window
    CheckType.HEADLIGHT -> Icons.Rounded.Highlight
    CheckType.PARKING_BRAKE -> Icons.Rounded.LocalParking
}

@Composable
private fun status(item: ParkingCheckItem): String = stringResource(
    when {
        item.isOk -> R.string.park_status_ok
        item.type == CheckType.WINDOW -> R.string.park_status_window
        item.type == CheckType.HEADLIGHT -> R.string.park_status_headlight
        else -> R.string.park_status_brake
    },
)

@Composable
private fun label(item: ParkingCheckItem): String = when (item.type) {
    CheckType.WINDOW -> item.area?.let { stringResource(R.string.check_window, areaName(it)) }
        ?: stringResource(R.string.check_window_all)
    CheckType.HEADLIGHT -> stringResource(R.string.check_headlight)
    CheckType.PARKING_BRAKE -> stringResource(R.string.check_parking_brake)
}

@Composable
private fun areaName(area: CabinArea): String = stringResource(
    when (area) {
        CabinArea.FRONT_LEFT -> R.string.area_front_left
        CabinArea.FRONT_RIGHT -> R.string.area_front_right
        CabinArea.REAR_LEFT -> R.string.area_rear_left
        CabinArea.REAR_RIGHT -> R.string.area_rear_right
    },
)
