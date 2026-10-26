package com.firstroad.mate.ui.park

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalParking
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firstroad.mate.R
import com.firstroad.mate.ui.common.BeginnerScaffold
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.Caption
import com.firstroad.mate.ui.common.CockpitCard
import com.firstroad.mate.ui.common.CockpitHeader
import com.firstroad.mate.ui.common.IconDisc
import com.firstroad.mate.ui.common.QuestionBar
import com.firstroad.mate.ui.common.VoiceDock
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
        topBar = { CockpitHeader(title = stringResource(R.string.park_title), isDemo = state.isDemo) },
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
 * 와이어프레임 D. 왼쪽은 하차 안심 체크(타일 3장)·질문·주차 위치, 넓은 화면이면 오른쪽에 오늘의 운전 리포트.
 * 아래는 늘 같은 자리의 음성 바(마이크 + 확인했어요). 메모도 같은 마이크로 말한다("주차 메모 B2 C14").
 */
@Composable
fun ParkScreen(state: ParkUiState, actions: ParkActions, modifier: Modifier = Modifier) = BoxWithConstraints(modifier.fillMaxSize()) {
    val dimens = FirstRoad.dimens
    val isWide = maxWidth >= dimens.splitMinWidth
    val isShort = maxHeight < dimens.shortScreenHeight
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenPadding, vertical = dimens.smallGap),
        verticalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            val scroll = rememberScrollState()
            // 질문이나 새 대답이 오면 그 자리가 보이게 맨 위로 올린다 (점검·질문·대답이 위쪽에 있다).
            LaunchedEffect(state.question, state.headline, state.heardText) { scroll.animateScrollTo(0) }
            Column(
                Modifier.weight(if (isWide) 1.5f else 1f).fillMaxHeight().verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(if (isShort) dimens.smallGap else dimens.gap),
            ) {
                if (!isShort) Caption(stringResource(R.string.park_checks_caption))
                // 질문이 떠 있으면 타일의 '닫기'는 질문의 '응'과 같은 일이라 숨겨, 질문이 바로 아래에 보이게 한다.
                CheckList(checks = state.checks, onFix = actions.onFix, showFixButtons = state.question == null)
                state.question?.let { QuestionBar(it, actions.onAnswer, compact = isShort) }
                ParkingCard(state)
                if (!isWide) ReportCard(state)
            }
            if (isWide) {
                Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) { ReportCard(state) }
            }
        }
        // 낮은 화면에서 질문이 떠 있으면 칩 줄을 숨겨 질문 카드가 잘리지 않게 한다 (질문에 응/아니 버튼이 있다).
        if ((state.isDemo || state.isVoiceUnavailable) && !(isShort && state.question != null)) {
            VoiceSimulationPanel(stringArrayResource(R.array.park_voice_samples).toList(), actions.onSimulate, singleRow = true)
        }
        VoiceDock(
            isListening = state.isListening,
            onMic = actions.onMic,
            hint = stringResource(R.string.voice_hint_park),
            heard = state.heardText,
            answer = state.headline?.takeIf { state.question == null },
        ) {
            BigActionButton(stringResource(R.string.park_done), actions.onDone, icon = Icons.Rounded.CheckCircle)
        }
    }
}

@Composable
private fun ReportCard(state: ParkUiState) {
    DriveReportCard(
        stats = state.stats,
        level = state.level,
        isLevelUp = state.isLevelUp,
        drivesToNextLevel = state.drivesToNextLevel,
    )
}

/** 주차 위치 카드: 저장 여부와 음성 메모. */
@Composable
private fun ParkingCard(state: ParkUiState) {
    if (!state.isParkingSaved && state.parkingMemo == null) return
    CockpitCard(modifier = Modifier.fillMaxWidth(), caption = stringResource(R.string.park_parking_caption), contentPadding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.smallGap)) {
            IconDisc(Icons.Rounded.LocalParking, MaterialTheme.colorScheme.primary)
            Column {
                if (state.isParkingSaved) Text(stringResource(R.string.park_location_saved), style = MaterialTheme.typography.titleMedium)
                state.parkingMemo?.let { Text(stringResource(R.string.park_memo, it), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

