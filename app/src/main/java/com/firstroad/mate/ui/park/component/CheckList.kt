package com.firstroad.mate.ui.park.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.CabinArea
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.ParkingCheckItem
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.Tone
import com.firstroad.mate.ui.common.toneColor
import com.firstroad.mate.ui.theme.FirstRoad

/** 와이어프레임 D 윗부분: ⚠️ 뒷좌석 왼쪽 창문 열림 [닫기]  ✅ 전조등  ✅ 주차 브레이크 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckList(checks: List<ParkingCheckItem>, onFix: (ParkingCheckItem) -> Unit, modifier: Modifier = Modifier) {
    val dimens = FirstRoad.dimens
    Column(modifier, verticalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.gap), verticalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
            checks.forEach { item -> CheckChip(item, onFix) }
        }
        if (checks.size < CheckType.entries.size) {
            Text(stringResource(R.string.park_skipped), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CheckChip(item: ParkingCheckItem, onFix: (ParkingCheckItem) -> Unit) {
    val tone = if (item.isOk) Tone.OK else Tone.PREPARE
    Card(
        shape = RoundedCornerShape(FirstRoad.dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            Modifier
                .heightIn(min = FirstRoad.dimens.minTouch)
                .padding(horizontal = FirstRoad.dimens.gap, vertical = FirstRoad.dimens.smallGap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.smallGap),
        ) {
            Text(if (item.isOk) "✅" else "⚠️", style = MaterialTheme.typography.titleLarge)
            Text(label(item), style = MaterialTheme.typography.titleLarge, color = toneColor(tone))
            if (!item.isOk && item.canFixRemotely) {
                BigActionButton(stringResource(R.string.park_fix_window), { onFix(item) })
            }
        }
    }
}

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
