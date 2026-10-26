package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firstroad.mate.R
import com.firstroad.mate.ui.theme.FirstRoad

/** 확인 질문: 말로 "응/아니"라고 해도 되고, 큰 버튼 한 번으로 답해도 된다. 시안 테두리로 '지금 대답할 것'을 표시한다. */
@Composable
fun QuestionBar(
    question: String,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CockpitCard(modifier = modifier.fillMaxWidth(), accent = MaterialTheme.colorScheme.primary, contentPadding = FirstRoad.dimens.gap) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.smallGap)) {
            Icon(Icons.Rounded.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text(question, style = MaterialTheme.typography.titleLarge)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
            BigActionButton(stringResource(R.string.answer_yes), { onAnswer(true) }, Modifier.weight(1f), icon = Icons.Rounded.Check)
            BigActionButton(stringResource(R.string.answer_no), { onAnswer(false) }, Modifier.weight(1f), isPrimary = false, icon = Icons.Rounded.Close)
        }
    }
}
