package com.firstroad.mate.ui.drive.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.ui.common.Caption
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.Numeral
import com.firstroad.mate.ui.common.SegmentBar
import com.firstroad.mate.ui.common.Tone
import com.firstroad.mate.ui.common.ToneBadge
import com.firstroad.mate.ui.common.TurnArrow
import com.firstroad.mate.ui.common.splitDistance
import com.firstroad.mate.ui.common.toneColor
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 와이어프레임 B: 주행 중 '다음 행동 카드' 한 장. 계기판의 길 안내처럼 큰 화살표 + 거리 숫자,
 * 그 아래 지금 할 일 한 문장. 카드 테두리 색이 상태(괜찮음·준비·지금)를 알린다.
 * - compact: 낮은 화면에서 숫자·글자를 줄이고 문장은 두 줄에서 줄임표로 끝낸다.
 * - slim: 낮은 화면에 경고·질문 카드가 함께 뜰 때 상태·화살표·문장 한 줄만 남긴다.
 */
@Composable
fun NextActionCard(
    tone: Tone,
    headline: String?,
    nextTurnText: String?,
    tension: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    slim: Boolean = false,
    turnDirection: TurnDirection? = null,
    turnMeters: Int? = null,
    showTension: Boolean = true,
) {
    val dimens = FirstRoad.dimens
    val accent = toneColor(tone)
    val sentence = headline ?: stringResource(R.string.drive_waiting)
    if (slim) {
        CockpitCard(modifier = modifier.fillMaxWidth(), accent = accent, contentPadding = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
                ToneBadge(tone)
                if (turnMeters != null) TurnArrow(turnDirection, Modifier.size(40.dp), accent)
                Text(sentence, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        return
    }
    CockpitCard(modifier = modifier.fillMaxWidth(), accent = accent, contentPadding = if (compact) 20.dp else dimens.cardPadding) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            Caption(stringResource(R.string.drive_next_caption), Modifier.weight(1f))
            if (showTension) TensionMini(tension)
            ToneBadge(tone)
        }
        if (compact) {
            // 낮은 화면: 화살표·거리·문장을 한 줄에.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                if (turnMeters != null) {
                    TurnArrow(turnDirection, Modifier.size(72.dp), accent)
                    val (value, unit) = splitDistance(turnMeters)
                    Numeral(value, unit)
                }
                Column(Modifier.weight(1f).padding(start = if (turnMeters != null) 8.dp else 0.dp)) {
                    Text(sentence, style = MaterialTheme.typography.headlineMedium, maxLines = COMPACT_MAX_LINES, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
            // 계기판처럼 큰 화살표와 거리 숫자를 위에, 할 일 문장을 아래에 크게.
            if (turnMeters != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    TurnArrow(turnDirection, Modifier.size(120.dp), accent)
                    val (value, unit) = splitDistance(turnMeters)
                    Numeral(value, unit, huge = true)
                }
            }
            Text(sentence, style = MaterialTheme.typography.displayLarge)
        }
        if (!compact) {
            nextTurnText?.let {
                Text("▶ $it", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** 카드 머리의 작은 긴장 지수: "긴장 ▬▬▭▭▭" — 60 이상이면 노란색. */
@Composable
private fun TensionMini(tension: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Caption(stringResource(R.string.drive_tension))
        SegmentBar(
            filled = (tension / 20).coerceIn(0, METER_STEPS),
            total = METER_STEPS,
            color = if (tension >= CALM_THRESHOLD) FirstRoad.tones.prepare else MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(120.dp),
        )
    }
}

private const val METER_STEPS = 5
private const val CALM_THRESHOLD = 60
private const val COMPACT_MAX_LINES = 2
