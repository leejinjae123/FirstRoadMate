package com.firstroad.mate.ui.drive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firstroad.mate.R
import com.firstroad.mate.ui.common.BeginnerScaffold
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.CockpitHeader
import com.firstroad.mate.ui.common.QuestionBar
import com.firstroad.mate.ui.common.VoiceDock
import com.firstroad.mate.ui.common.VoiceSimulationPanel
import com.firstroad.mate.ui.common.formatDistance
import com.firstroad.mate.ui.drive.component.NextActionCard
import com.firstroad.mate.ui.drive.component.RemainingWidget
import com.firstroad.mate.ui.drive.component.SpeedWidget
import com.firstroad.mate.ui.drive.component.TensionWidget
import com.firstroad.mate.ui.drive.component.UpcomingTurnsCard
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

/** 머리: 목적지 + (데모) + 오른쪽에 시속·남은 거리. 넓은 화면에서는 위젯 열이 같은 정보를 크게 보여 준다. */
@Composable
private fun DriveTopBar(state: DriveUiState) {
    CockpitHeader(title = state.destinationName, isDemo = state.isDemo) {
        state.speedKmh?.let { Text(stringResource(R.string.drive_speed, it), style = MaterialTheme.typography.titleLarge) }
        state.remainingMeters?.let {
            Text(
                "   " + stringResource(R.string.drive_remaining, formatDistance(it)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 와이어프레임 B·C. 계기판처럼 왼쪽은 다음 행동(화살표·거리·할 일)과 경고·질문 카드, 넓은 화면이면 오른쪽에 속도·긴장·남은 거리 위젯.
 * 주행 중에는 스크롤 없이 한 화면에 담고, 마이크는 항상 맨 아래 같은 자리에 둔다.
 * 질문이나 경고 카드가 떠 있으면 그 카드의 버튼 2개와 마이크만 남긴다 (기획서 6.1 '하나씩').
 */
@Composable
fun DriveScreen(state: DriveUiState, actions: DriveActions, modifier: Modifier = Modifier) = BoxWithConstraints(modifier.fillMaxSize()) {
    val dimens = FirstRoad.dimens
    val hasCardActions = state.question != null || state.warning != null
    val isWide = maxWidth >= dimens.splitMinWidth
    // 화면이 낮으면(예: 1024×768) 카드 글자를 줄이고, 경고·질문 카드가 뜨면 다음 행동 카드를 한 줄로 줄여
    // 경고·질문 카드가 밀려나지 않게 한다.
    val isShort = maxHeight < dimens.shortScreenHeight
    // 데모 음성 칩("휴게소 넣어 줘", "나중에")은 경고 카드 버튼과 같은 일을 한다. 낮은 화면에서는 칩을 남기고 카드 버튼을 접는다.
    // 확인 질문이 떠 있을 때만 칩을 숨긴다 (질문 카드에 응/아니 버튼이 있다).
    val showChips = (state.isDemo || state.isVoiceUnavailable) && !(isShort && state.question != null)
    val showWarningActions = state.question == null && !(isShort && showChips)
    // 다음 행동 카드를 한 줄로 줄이는 것은 경고 카드에 버튼까지 있어 자리가 모자랄 때만.
    val isSlim = isShort && state.warning != null && showWarningActions
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenPadding, vertical = dimens.smallGap),
        verticalArrangement = Arrangement.spacedBy(if (isShort) dimens.smallGap else dimens.gap),
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            Column(Modifier.weight(if (isWide) 1.7f else 1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(if (isShort) dimens.smallGap else dimens.gap)) {
                // 낮은 화면에서 확인 질문(7초)이 떠 있는 동안은 질문 버튼을 위해 다음 행동 카드를 잠시 숨긴다. 회전 안내는 음성으로 계속된다.
                if (!(isShort && state.question != null)) {
                    NextActionCard(
                        tone = state.tone,
                        headline = state.headline,
                        nextTurnText = state.nextTurnText,
                        tension = state.tension,
                        compact = isShort,
                        slim = isSlim,
                        turnDirection = state.nextTurnDirection,
                        turnMeters = state.nextTurnMeters,
                        showTension = !isWide,
                    )
                }
                state.warning?.let {
                    WarningCard(it, showActions = showWarningActions, actions.onAddSuggestedStop, actions.onDismissWarning, compact = isShort)
                }
                state.question?.let { QuestionBar(it, actions.onAnswer, compact = isShort) }
                // 경고·질문이 없을 때만 이어지는 갈림길 예습 (카드가 겹쳐 잘리지 않게). 들은 말·대답은 음성 바에.
                if (!hasCardActions && !isShort && isWide) UpcomingTurnsCard(state.upcomingTurns, state.speedKmh)
            }
            if (isWide) {
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    SpeedWidget(state.speedKmh, state.speedLimitKmh)
                    TensionWidget(state.tension, state.isCalmActive)
                    RemainingWidget(state.remainingMeters, state.remainingMinutes, state.destinationName)
                }
            }
        }
        if (showChips) {
            VoiceSimulationPanel(stringArrayResource(R.array.drive_voice_samples).toList(), actions.onSimulate, singleRow = true)
        }
        VoiceDock(
            isListening = state.isListening,
            onMic = actions.onMic,
            hint = stringResource(R.string.voice_hint_drive),
            heard = state.heardText,
            answer = state.answer,
        ) {
            if (!hasCardActions) {
                // 정차 중(제한 없음)에는 '주행 끝내기', 주행 중에는 '다시 듣기'.
                if (!state.isRestricted && state.isStarted) {
                    BigActionButton(stringResource(R.string.drive_end), actions.onEndDrive, isPrimary = false, icon = Icons.Rounded.StopCircle)
                } else {
                    BigActionButton(stringResource(R.string.drive_repeat), actions.onRepeat, isPrimary = false, icon = Icons.Rounded.Replay)
                }
            }
        }
    }
}
