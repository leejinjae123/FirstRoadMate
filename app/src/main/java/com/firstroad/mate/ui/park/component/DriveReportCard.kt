package com.firstroad.mate.ui.park.component

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
import com.firstroad.mate.domain.model.DriveStats
import com.firstroad.mate.ui.common.LevelBadge
import com.firstroad.mate.ui.theme.FirstRoad

/** 와이어프레임 D 아랫부분: 오늘의 운전 + 레벨. 잘한 것을 먼저 보여 준다. */
@Composable
fun DriveReportCard(
    stats: DriveStats?,
    level: Int,
    isLevelUp: Boolean,
    drivesToNextLevel: Int?,
    modifier: Modifier = Modifier,
) {
    val dimens = FirstRoad.dimens
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
            Text(stringResource(R.string.park_report_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            stats?.let {
                val items = listOf(
                    stringResource(R.string.park_difficult, it.difficultPassed),
                    stringResource(R.string.park_signal, it.signalUsed, it.signalTotal),
                    stringResource(R.string.park_hard_brake, it.hardBrakes),
                )
                Text(items.joinToString("  ·  "), style = MaterialTheme.typography.headlineMedium)
            }
            if (isLevelUp) {
                Text(stringResource(R.string.park_level_up, level), style = MaterialTheme.typography.headlineMedium)
            } else {
                LevelBadge(level)
                drivesToNextLevel?.let {
                    Text(
                        stringResource(R.string.park_next_level, level + 1, it),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
