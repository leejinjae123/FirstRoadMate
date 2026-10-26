package com.firstroad.mate.ui.drive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firstroad.mate.R
import com.firstroad.mate.ui.common.BeginnerScaffold
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.DemoBadge
import com.firstroad.mate.ui.common.QuestionBar
import com.firstroad.mate.ui.common.VoiceSimulationPanel
import com.firstroad.mate.ui.common.formatDistance
import com.firstroad.mate.ui.drive.component.NextActionCard
import com.firstroad.mate.ui.drive.component.WarningCard
import com.firstroad.mate.ui.theme.FirstRoad

@Composable
fun DriveRoute(
    onFinished: () -> Unit,
    viewModel: DriveViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isFinished) { if (state.isFinished) onFinished() }
    BeginnerScaffold(
        toastEvents = viewModel.toastEvents,
        isLoading = false,
        topBar = { DriveTopBar(state) },
    ) { padding ->
        DriveScreen(
            state = state,
            actions = DriveActions(
                onMic = viewModel::onMicClick,
                onRepeat = viewModel::onRepeat,
                onAnswer = viewModel::onAnswer,
                onAddSuggestedStop = viewModel::onAddSuggestedStop,
                onDismissWarning = viewModel::onDismissWarning,
                onSimulate = viewModel::onSimulatedUtterance,
                onEndDrive = viewModel::onEndDrive,
            ),
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun DriveTopBar(state: DriveUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = FirstRoad.dimens.smallGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap),
    ) {
        Text(state.destinationName, style = MaterialTheme.typography.headlineMedium)
        if (state.isDemo) DemoBadge()
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            state.speedKmh?.let { Text(stringResource(R.string.drive_speed, it), style = MaterialTheme.typography.titleLarge) }
            state.remainingMeters?.let {
                Spacer(Modifier.width(FirstRoad.dimens.gap))
                Text(
                    stringResource(R.string.drive_remaining, formatDistance(it)),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * 와이어프레임 B·C. 주행 중 스크롤 없이 한 화면에 담고, 마이크는 항상 맨 아래 같은 자리에 둔다.
 * 질문이나 경고 카드가 떠 있으면 그 카드의 버튼 2개와 마이크만 남긴다 (기획서 6.1 '하나씩').
 */
@Composable
fun DriveScreen(state: DriveUiState, actions: DriveActions, modifier: Modifier = Modifier) {
    val dimens = FirstRoad.dimens
    val hasCardActions = state.question != null || state.warning != null
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenPadding, vertical = dimens.gap),
        verticalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
            NextActionCard(
                tone = state.tone,
                headline = state.headline,
                nextTurnText = state.nextTurnText,
                tension = state.tension,
            )
            state.warning?.let {
                WarningCard(it, showActions = state.question == null, actions.onAddSuggestedStop, actions.onDismissWarning)
            }
            state.question?.let { QuestionBar(it, actions.onAnswer) }
            state.heardText?.let {
                Text(stringResource(R.string.voice_heard, it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.isDemo || state.isVoiceUnavailable) {
            VoiceSimulationPanel(stringArrayResource(R.array.drive_voice_samples).toList(), actions.onSimulate)
        }
        BottomVoiceBar(state, actions, showSecondary = !hasCardActions)
    }
}

@Composable
private fun BottomVoiceBar(state: DriveUiState, actions: DriveActions, showSecondary: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
        BigActionButton(
            text = stringResource(if (state.isListening) R.string.voice_listening else R.string.voice_speak),
            onClick = actions.onMic,
            modifier = Modifier.weight(1f),
        )
        if (showSecondary) {
            // 정차 중(제한 없음)에는 '주행 끝내기', 주행 중에는 '다시 듣기'.
            if (!state.isRestricted && state.isStarted) {
                BigActionButton(stringResource(R.string.drive_end), actions.onEndDrive, Modifier.weight(1f), isPrimary = false)
            } else {
                BigActionButton(stringResource(R.string.drive_repeat), actions.onRepeat, Modifier.weight(1f), isPrimary = false)
            }
        }
    }
}
