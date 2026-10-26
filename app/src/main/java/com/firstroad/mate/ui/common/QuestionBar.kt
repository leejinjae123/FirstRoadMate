package com.firstroad.mate.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.firstroad.mate.R
import com.firstroad.mate.ui.theme.FirstRoad

/** 확인 질문: 말로 "응/아니"라고 해도 되고, 큰 버튼 한 번으로 답해도 된다. */
@Composable
fun QuestionBar(
    question: String,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(FirstRoad.dimens.smallGap)) {
        Text(question, style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
            BigActionButton(stringResource(R.string.answer_yes), { onAnswer(true) }, Modifier.weight(1f))
            BigActionButton(stringResource(R.string.answer_no), { onAnswer(false) }, Modifier.weight(1f), isPrimary = false)
        }
    }
}
