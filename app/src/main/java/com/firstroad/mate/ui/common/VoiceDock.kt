package com.firstroad.mate.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.ui.theme.FirstRoad

/**
 * 모든 화면 맨 아래의 음성 바: 왼쪽 끝에 늘 같은 자리의 큰 마이크, 가운데에 '이렇게 말해 보세요',
 * 오른쪽에 그 화면의 주 버튼. 차량 화면의 음성 비서 바처럼 손이 기억하는 위치를 바꾸지 않는다.
 */
@Composable
fun VoiceDock(
    isListening: Boolean,
    onMic: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    heard: String? = null,
    answer: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val dimens = FirstRoad.dimens
    val shape = RoundedCornerShape(dimens.micSize / 2 + 12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimens.micSize + 16.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(dimens.hairline, MaterialTheme.colorScheme.outline, shape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        MicButton(isListening, onMic)
        // 음성 비서 바처럼: 듣는 중 → "듣고 있어요…", 대답이 있으면 들은 말과 대답, 없으면 '이렇게 말해 보세요' 예시.
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Caption(
                when {
                    isListening -> stringResource(R.string.voice_listening)
                    heard != null -> stringResource(R.string.voice_heard, heard)
                    else -> stringResource(R.string.voice_speak_caption)
                },
                color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                answer ?: hint,
                style = MaterialTheme.typography.bodyMedium,
                color = if (answer != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing()
    }
}

/** 시안 원형 마이크. 듣는 동안 바깥 고리가 숨 쉬듯 커졌다 작아진다. */
@Composable
private fun MicButton(isListening: Boolean, onClick: () -> Unit) {
    val size = FirstRoad.dimens.micSize
    val pulse by rememberInfiniteTransition(label = "mic").animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
        label = "micPulse",
    )
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (isListening) {
            Box(
                Modifier
                    .size(size)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
            )
        }
        Box(
            Modifier
                .size(size - 12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.voice_speak), onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isListening) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                contentDescription = stringResource(R.string.voice_speak),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}

private const val PULSE_MILLIS = 700
