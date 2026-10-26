package com.firstroad.mate.ui.park.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.DriveStats
import com.firstroad.mate.domain.model.LevelTier
import com.firstroad.mate.ui.common.Caption
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.RingProgress
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 와이어프레임 D 아랫부분: 오늘의 운전. 잘한 것(긴장 구간 통과)을 먼저, 숫자는 계기판처럼 크게.
 * 깜빡이 사용률은 고리로 보여 준다. 아래에 레벨 고리와 다음 레벨까지 남은 주행.
 */
@Composable
fun DriveReportCard(
    stats: DriveStats?,
    level: Int,
    isLevelUp: Boolean,
    drivesToNextLevel: Int?,
    modifier: Modifier = Modifier,
) {
    val dimens = FirstRoad.dimens
    CockpitCard(modifier = modifier.fillMaxWidth(), caption = stringResource(R.string.park_report_title)) {
        stats?.let {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap), verticalAlignment = Alignment.CenterVertically) {
                Stat(stringResource(R.string.park_difficult_caption), stringResource(R.string.park_spots, it.difficultPassed), FirstRoad.tones.ok, Modifier.weight(1f))
                val signalRate = if (it.signalTotal == 0) 0f else it.signalUsed / it.signalTotal.toFloat()
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    RingProgress(signalRate, Modifier.size(96.dp), strokeWidth = 9.dp) {
                        Text("${it.signalUsed}/${it.signalTotal}", style = MaterialTheme.typography.titleLarge)
                    }
                    Caption(stringResource(R.string.park_signal_caption))
                }
                Stat(
                    stringResource(R.string.park_brake_caption),
                    stringResource(R.string.park_count, it.hardBrakes),
                    if (it.hardBrakes == 0) FirstRoad.tones.ok else FirstRoad.tones.prepare,
                    Modifier.weight(1f),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            RingProgress(level / LevelTier.entries.size.toFloat(), Modifier.size(72.dp), strokeWidth = 7.dp) {
                Text("$level", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Column {
                if (isLevelUp) {
                    Text(stringResource(R.string.park_level_up, level), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(stringResource(R.string.home_level, level), style = MaterialTheme.typography.titleLarge)
                    drivesToNextLevel?.let {
                        Text(stringResource(R.string.park_next_level, level + 1, it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(caption: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(value, style = FirstRoad.type.numeralSmall, color = color)
        Caption(caption)
    }
}
