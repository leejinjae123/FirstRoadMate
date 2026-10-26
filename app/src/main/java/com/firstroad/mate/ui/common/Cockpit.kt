package com.firstroad.mate.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.ui.theme.FirstRoad

/*
 * 콕핏 디자인 시스템의 공통 부품. 차량 화면의 '홈 카드'처럼
 * 둥근 모서리 + 얇은 테두리 카드, 작고 흐린 제목, 큰 숫자를 같은 방식으로 그린다.
 */

/** 위젯 카드. caption은 "다음 안내", "내 차"처럼 작은 제목. accent를 주면 테두리를 그 색으로 강조한다. */
@Composable
fun CockpitCard(
    modifier: Modifier = Modifier,
    caption: String? = null,
    accent: Color? = null,
    contentPadding: Dp = FirstRoad.dimens.cardPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = FirstRoad.dimens
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (accent != null) 2.dp else dimens.hairline, accent ?: MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(contentPadding), verticalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
            caption?.let { Caption(it) }
            content()
        }
    }
}

@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, modifier = modifier, style = FirstRoad.type.caption, color = color)
}

/** 큰 숫자 + 단위: "350 m", "60 km/h". */
@Composable
fun Numeral(value: String, unit: String?, modifier: Modifier = Modifier, huge: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(value, style = if (huge) FirstRoad.type.numeralHuge else FirstRoad.type.numeral, color = color)
        unit?.let {
            Text(it, style = FirstRoad.type.unit, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = if (huge) 14.dp else 8.dp))
        }
    }
}

/** 둥근 아이콘 칩: 원 안에 아이콘. 목적지·경유지·점검 항목에 같은 모양을 쓴다. */
@Composable
fun IconDisc(icon: ImageVector, tint: Color, modifier: Modifier = Modifier, size: Dp = 56.dp, background: Color = tint.copy(alpha = 0.16f)) {
    Box(modifier.size(size).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

/**
 * 회전 화살표 (계기판의 TBT처럼). 아이콘 폰트가 아니라 선으로 그려 어떤 차량 화면에서도 같은 모양이 나온다.
 */
@Composable
fun TurnArrow(direction: TurnDirection?, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path()
        val head = Path()
        val headSize = w * 0.22f
        when (direction) {
            TurnDirection.LEFT, TurnDirection.RIGHT -> {
                val sign = if (direction == TurnDirection.RIGHT) 1f else -1f
                val cx = w / 2f
                val tipX = cx + sign * w * 0.36f
                val turnY = h * 0.36f
                path.moveTo(cx - sign * w * 0.06f, h * 0.92f)
                path.lineTo(cx - sign * w * 0.06f, turnY + w * 0.16f)
                path.quadraticTo(cx - sign * w * 0.06f, turnY, cx + sign * w * 0.12f, turnY)
                path.lineTo(tipX - sign * headSize * 0.6f, turnY)
                head.moveTo(tipX - sign * headSize, turnY - headSize)
                head.lineTo(tipX, turnY)
                head.lineTo(tipX - sign * headSize, turnY + headSize)
            }
            TurnDirection.U_TURN -> {
                path.moveTo(w * 0.66f, h * 0.92f)
                path.lineTo(w * 0.66f, h * 0.36f)
                path.cubicTo(w * 0.66f, h * 0.06f, w * 0.30f, h * 0.06f, w * 0.30f, h * 0.36f)
                path.lineTo(w * 0.30f, h * 0.70f)
                head.moveTo(w * 0.30f - headSize, h * 0.70f - headSize)
                head.lineTo(w * 0.30f, h * 0.80f)
                head.lineTo(w * 0.30f + headSize, h * 0.70f - headSize)
            }
            TurnDirection.STRAIGHT, null -> {
                path.moveTo(w / 2f, h * 0.92f)
                path.lineTo(w / 2f, h * 0.14f)
                head.moveTo(w / 2f - headSize, h * 0.14f + headSize)
                head.lineTo(w / 2f, h * 0.08f)
                head.lineTo(w / 2f + headSize, h * 0.14f + headSize)
            }
        }
        drawPath(path, color, style = stroke)
        drawPath(head, color, style = stroke)
    }
}

/** 원호 게이지 (속도·긴장 지수). value는 0..1, 240도 원호에 그린다. 가운데는 content. */
@Composable
fun ArcGauge(
    value: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outline,
    strokeWidth: Dp = 14.dp,
    content: @Composable () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(trackColor, START_ANGLE, SWEEP, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            val sweep = SWEEP * value.coerceIn(0f, 1f)
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.55f), color, color.copy(alpha = 0.55f))),
                    startAngle = START_ANGLE,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** 고리 진행률 (레벨, 깜빡이 사용률). */
@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outline,
    strokeWidth: Dp = 10.dp,
    content: @Composable () -> Unit = {},
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            drawArc(trackColor, 0f, 360f, false, topLeft, Size(diameter, diameter), style = Stroke(stroke))
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, topLeft, Size(diameter, diameter), style = Stroke(stroke, cap = StrokeCap.Round))
        }
        content()
    }
}

/** 칸 막대: 긴장 지수 ■■■□□ 를 도형으로. */
@Composable
fun SegmentBar(filled: Int, total: Int, color: Color, modifier: Modifier = Modifier, height: Dp = 14.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(height / 2))
                    .background(if (index < filled) color else MaterialTheme.colorScheme.outline),
            )
        }
    }
}

/** 가로 막대 게이지 (연료). */
@Composable
fun LevelBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 12.dp) {
    Box(modifier.height(height).clip(RoundedCornerShape(height / 2)).background(MaterialTheme.colorScheme.outline)) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color),
        )
    }
}

/** 한 줄 정보: [아이콘] 이름 ........ 값 */
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.smallGap)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}

/** 화면 위쪽 머리: 제목 + (데모 배지) + 오른쪽 끝 내용. */
@Composable
fun CockpitHeader(title: String, isDemo: Boolean, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = FirstRoad.dimens.smallGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap),
    ) {
        Box(Modifier.width(6.dp).height(32.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
        Text(title, style = MaterialTheme.typography.headlineLarge)
        if (isDemo) DemoBadge()
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { trailing() }
    }
}

/** 강조 테두리 + 얇은 외곽선 카드에 쓰는 미세한 윤곽. */
fun Modifier.hairline(color: Color, radius: Dp): Modifier = border(1.dp, color, RoundedCornerShape(radius))

private const val START_ANGLE = 150f
private const val SWEEP = 240f
