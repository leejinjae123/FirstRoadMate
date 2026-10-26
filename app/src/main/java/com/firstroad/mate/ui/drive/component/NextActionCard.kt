package com.firstroad.mate.ui.drive.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.ui.common.Tone
import com.firstroad.mate.ui.common.ToneBadge
import com.firstroad.mate.ui.common.toneColor
import com.firstroad.mate.ui.theme.FirstRoad

/** 와이어프레임 B: 주행 중 화면의 '다음 행동 카드' 한 장. 메시지 하나, 2초 안에 읽히게. */
@Composable
fun NextActionCard(
    tone: Tone,
    headline: String?,
    nextTurnText: String?,
    tension: Int,
    modifier: Modifier = Modifier,
) {
    val dimens = FirstRoad.dimens
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cornerRadius),
        border = BorderStroke(4.dp, toneColor(tone)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                ToneBadge(tone)
                TensionMeter(tension, Modifier.weight(1f))
            }
            Text(
                text = headline ?: stringResource(R.string.drive_waiting),
                style = MaterialTheme.typography.displayLarge,
            )
            nextTurnText?.let {
                Text("▶ $it", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** "긴장 지수 ■■■□□" — 글리프 대신 도형으로 그려 어떤 차량 폰트에서도 깨지지 않게 한다. */
@Composable
private fun TensionMeter(tension: Int, modifier: Modifier = Modifier) {
    val filled = (tension / 20).coerceIn(0, METER_STEPS)
    val activeColor = if (tension >= 60) FirstRoad.tones.prepare else MaterialTheme.colorScheme.primary
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.drive_tension),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        repeat(METER_STEPS) { index ->
            Box(
                Modifier
                    .size(width = 20.dp, height = 32.dp)
                    .background(
                        color = if (index < filled) activeColor else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

private const val METER_STEPS = 5
