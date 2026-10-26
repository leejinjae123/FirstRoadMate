package com.firstroad.mate.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R

/**
 * 음성 시뮬레이션: 음성 인식이 없는 일반 에뮬레이터·심사 환경에서
 * 기획서 4.2의 음성 명령을 버튼으로 대신 말해 볼 수 있게 한다.
 * 화면이 낮으면(singleRow) 한 줄로 두고 옆으로 넘겨 본다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSimulationPanel(
    samples: List<String>,
    onSay: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleRow: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val title = @Composable {
            Text(
                text = stringResource(R.string.voice_simulation_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // 한 줄일 때는 제목도 같은 줄 맨 앞에 둬 높이를 아낀다.
        if (!singleRow) title()
        val chips = @Composable {
            samples.forEach { sample ->
                AssistChip(
                    onClick = { onSay(sample) },
                    label = { Text("\"$sample\"", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.heightIn(min = 56.dp),
                )
            }
        }
        if (singleRow) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title()
                chips()
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
        }
    }
}

/** 마이크 버튼 위에 두는 한 줄 예시: 무엇이라고 말하면 되는지 바로 보이게. */
@Composable
fun VoiceHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** 들은 말과 앱의 대답. 음성으로만 조작해도 무엇이 처리됐는지 화면에서 확인할 수 있다. */
@Composable
fun VoiceEcho(heard: String?, answer: String?, modifier: Modifier = Modifier) {
    if (heard == null && answer == null) return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        heard?.let {
            Text(stringResource(R.string.voice_heard, it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        answer?.let { Text(it, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
    }
}
