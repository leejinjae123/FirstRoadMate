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
import com.firstroad.mate.ui.common.VoiceEcho
import com.firstroad.mate.ui.common.VoiceHint
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

/**
 * 와이어프레임 D. 위는 점검 결과·질문·리포트, 아래는 늘 같은 자리의 말하기·확인했어요.
 * 메모도 같은 마이크로 말한다("주차 메모 B2 C14"). 마이크 버튼을 하나로 둬 헷갈리지 않게 한다.
 */
@Composable
fun ParkScreen(state: ParkUiState, actions: ParkActions, modifier: Modifier = Modifier) {
    val dimens = FirstRoad.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenPadding, vertical = dimens.gap),
        verticalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        val scroll = rememberScrollState()
        // 질문이나 새 대답이 오면 그 자리가 보이게 맨 위로 올린다 (점검·질문·대답이 위쪽에 있다).
        LaunchedEffect(state.question, state.headline, state.heardText) { scroll.animateScrollTo(0) }
        Column(
            Modifier.weight(1f).verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(dimens.gap),
        ) {
            CheckList(checks = state.checks, onFix = actions.onFix)
            state.question?.let { QuestionBar(it, actions.onAnswer) }
            VoiceEcho(heard = state.heardText, answer = state.headline?.takeIf { state.question == null })
            ParkingLocation(state)
            DriveReportCard(
                stats = state.stats,
                level = state.level,
                isLevelUp = state.isLevelUp,
                drivesToNextLevel = state.drivesToNextLevel,
            )
        }
        if (state.isDemo || state.isVoiceUnavailable) {
            VoiceSimulationPanel(stringArrayResource(R.array.park_voice_samples).toList(), actions.onSimulate, singleRow = true)
        } else {
            VoiceHint(stringResource(R.string.voice_hint_park))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            BigActionButton(
                text = stringResource(if (state.isListening) R.string.voice_listening else R.string.voice_speak),
                onClick = actions.onMic,
                modifier = Modifier.weight(1f),
                isPrimary = false,
            )
            BigActionButton(stringResource(R.string.park_done), actions.onDone, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ParkingLocation(state: ParkUiState) {
    Column {
        if (state.isParkingSaved) {
            Text(stringResource(R.string.park_location_saved), style = MaterialTheme.typography.titleLarge)
        }
        state.parkingMemo?.let { Text(stringResource(R.string.park_memo, it), style = MaterialTheme.typography.bodyLarge) }
    }
}
