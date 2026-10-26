package com.firstroad.mate.ui.park

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.firstroad.mate.ui.park.component.CheckList
import com.firstroad.mate.ui.park.component.DriveReportCard
import com.firstroad.mate.ui.theme.FirstRoad

@Composable
fun ParkRoute(
    onDone: () -> Unit,
    viewModel: ParkViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    // 뒤로 가기도 '확인했어요'와 같이 경로를 정리하고 홈으로 간다.
    BackHandler(onBack = viewModel::onDone)
    BeginnerScaffold(
        toastEvents = viewModel.toastEvents,
        isLoading = state.isChecking,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = FirstRoad.dimens.smallGap),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap),
            ) {
                Text(stringResource(R.string.park_title), style = MaterialTheme.typography.headlineLarge)
                if (state.isDemo) DemoBadge()
            }
        },
    ) { padding ->
        ParkScreen(
            state = state,
            actions = ParkActions(
                onFix = viewModel::onFix,
                onAnswer = viewModel::onAnswer,
                onMic = viewModel::onMicClick,
                onSimulate = viewModel::onSimulatedUtterance,
                onDone = viewModel::onDone,
            ),
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun ParkScreen(state: ParkUiState, actions: ParkActions, modifier: Modifier = Modifier) {
    val dimens = FirstRoad.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.screenPadding, vertical = dimens.gap),
        verticalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        CheckList(checks = state.checks, onFix = actions.onFix)
        ParkingLocationRow(state, actions.onMic)
        state.question?.let { QuestionBar(it, actions.onAnswer) }
        state.headline?.takeIf { state.question == null }?.let {
            Text(it, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
        state.heardText?.let {
            Text(stringResource(R.string.voice_heard, it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DriveReportCard(
            stats = state.stats,
            level = state.level,
            isLevelUp = state.isLevelUp,
            drivesToNextLevel = state.drivesToNextLevel,
        )
        if (state.isDemo || state.isVoiceUnavailable) {
            VoiceSimulationPanel(stringArrayResource(R.array.park_voice_samples).toList(), actions.onSimulate)
        }
        BigActionButton(stringResource(R.string.park_done), actions.onDone, Modifier.fillMaxWidth())
    }
}

@Composable
private fun ParkingLocationRow(state: ParkUiState, onMic: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap)) {
        Column(Modifier.weight(1f)) {
            if (state.isParkingSaved) {
                Text(stringResource(R.string.park_location_saved), style = MaterialTheme.typography.titleLarge)
            }
            state.parkingMemo?.let { Text(stringResource(R.string.park_memo, it), style = MaterialTheme.typography.bodyLarge) }
        }
        BigActionButton(
            text = stringResource(if (state.isListening) R.string.voice_listening else R.string.park_memo_button),
            onClick = onMic,
            isPrimary = false,
        )
    }
}
