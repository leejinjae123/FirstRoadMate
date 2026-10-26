package com.firstroad.mate.ui.drive.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.Caption
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.IconDisc
import com.firstroad.mate.ui.common.ToneBadge
import com.firstroad.mate.ui.common.icon
import com.firstroad.mate.ui.common.toTone
import com.firstroad.mate.ui.common.toneColor
import com.firstroad.mate.ui.drive.WarningCardUi
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 와이어프레임 C: 경고등 통역사 카드. 등급 · 쉬운 설명 · 할 일. 테두리와 아이콘이 등급 색을 따른다.
 * @param showActions 확인 질문이 떠 있으면 버튼은 질문 쪽에만 두고 여기서는 숨긴다 (한 번에 버튼 2개).
 * @param compact 낮은 화면: 아이콘·등급·설명을 한 줄에 두어 버튼까지 화면 안에 들어오게 한다.
 */
@Composable
fun WarningCard(
    warning: WarningCardUi,
    showActions: Boolean,
    onAddSuggestedStop: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val dimens = FirstRoad.dimens
    val tone = warning.level.toTone()
    val color = toneColor(tone)
    CockpitCard(modifier = modifier.fillMaxWidth(), accent = color, contentPadding = if (compact) 18.dp else dimens.cardPadding) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
            IconDisc(Icons.Rounded.ReportProblem, color, size = if (compact) 48.dp else 64.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!compact) Caption(stringResource(R.string.drive_warning_caption))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
                    ToneBadge(tone, label = stringResource(warning.level.titleRes()))
                    if (compact) {
                        Text(warning.message, style = MaterialTheme.typography.bodyMedium, maxLines = COMPACT_MAX_LINES, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (!compact) Text(warning.message, style = MaterialTheme.typography.headlineMedium)
            }
        }
        if (showActions) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
                warning.suggestedStop?.let { stop ->
                    BigActionButton(
                        stringResource(R.string.drive_add_stop, stringResource(stop.nameRes())),
                        onAddSuggestedStop,
                        Modifier.weight(1f),
                        icon = stop.icon(),
                    )
                }
                BigActionButton(stringResource(R.string.drive_later), onDismiss, Modifier.weight(1f), isPrimary = false, icon = Icons.Rounded.Schedule)
            }
        }
    }
}

private const val COMPACT_MAX_LINES = 3

private fun WarningLevel.titleRes(): Int = when (this) {
    WarningLevel.STOP_NOW -> R.string.level_stop_title
    WarningLevel.CHECK_TODAY -> R.string.level_today_title
    WarningLevel.INFO -> R.string.level_info_title
}

private fun PlaceCategory.nameRes(): Int = when (this) {
    PlaceCategory.GAS_STATION -> R.string.cat_gas
    PlaceCategory.EV_CHARGER -> R.string.cat_ev
    PlaceCategory.PARKING -> R.string.cat_parking
    PlaceCategory.REST_AREA -> R.string.cat_rest
    PlaceCategory.REPAIR_SHOP -> R.string.cat_repair
    PlaceCategory.DESTINATION -> R.string.cat_destination
}
