package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R

/**
 * 음성 시뮬레이션: 음성 인식이 없는 일반 에뮬레이터·심사 환경에서
 * 기획서 4.2의 음성 명령을 버튼으로 대신 말해 볼 수 있게 한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSimulationPanel(
    samples: List<String>,
    onSay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.voice_simulation_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            samples.forEach { sample ->
                AssistChip(
                    onClick = { onSay(sample) },
                    label = { Text("\"$sample\"", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.heightIn(min = 56.dp),
                )
            }
        }
    }
}
