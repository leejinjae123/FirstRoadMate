package com.firstroad.mate.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.ui.theme.FirstRoad

/** 색 + 모양 + 문구를 함께 보여 주는 상태 배지. */
@Composable
fun ToneBadge(tone: Tone, modifier: Modifier = Modifier, label: String? = null) {
    val color = toneColor(tone)
    Row(
        modifier = modifier
            .heightIn(min = FirstRoad.dimens.badgeHeight)
            .background(color, RoundedCornerShape(FirstRoad.dimens.cornerRadius))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(tone.symbol, color = FirstRoad.tones.onTone, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(10.dp))
        Text(label ?: defaultLabel(tone), color = FirstRoad.tones.onTone, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun toneColor(tone: Tone): Color = when (tone) {
    Tone.OK -> FirstRoad.tones.ok
    Tone.PREPARE -> FirstRoad.tones.prepare
    Tone.ACTION -> FirstRoad.tones.action
}

@Composable
private fun defaultLabel(tone: Tone): String = stringResource(
    when (tone) {
        Tone.OK -> R.string.tone_ok
        Tone.PREPARE -> R.string.tone_prepare
        Tone.ACTION -> R.string.tone_action
    },
)
