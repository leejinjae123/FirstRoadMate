package com.firstroad.mate.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.ui.common.BeginnerScaffold
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.DemoBadge
import com.firstroad.mate.ui.common.LevelBadge
import com.firstroad.mate.ui.common.VoiceEcho
import com.firstroad.mate.ui.common.VoiceHint
import com.firstroad.mate.ui.common.VoiceSimulationPanel
import com.firstroad.mate.ui.home.component.RoutePlanCard
import com.firstroad.mate.ui.theme.FirstRoad

@Composable
fun HomeRoute(
    onTripStarted: (Place) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.startedTrip) {
        state.startedTrip?.let {
            onTripStarted(it)
            viewModel.onTripStartHandled()
        }
    }
    BeginnerScaffold(
        toastEvents = viewModel.toastEvents,
        isLoading = state.isBuilding,
        topBar = { HomeTopBar(level = state.level, isDemo = state.isDemo) },
    ) { padding ->
        HomeScreen(
            state = state,
            onSelectDestination = viewModel::onSelectDestination,
            onMic = viewModel::onMicClick,
            onStart = viewModel::onStart,
            onRemoveWaypoints = viewModel::onRemoveWaypoints,
            onSimulate = viewModel::onSimulatedUtterance,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun HomeTopBar(level: Int, isDemo: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = FirstRoad.dimens.smallGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FirstRoad.dimens.gap),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        if (isDemo) DemoBadge()
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) { LevelBadge(level) }
    }
}

/**
 * 와이어프레임 A. 위는 고르는 곳(목적지·추천 경로), 아래는 늘 같은 자리의 행동 버튼(말하기·출발하기).
 * 화면이 낮아도 '출발하기'가 스크롤 아래로 숨지 않는다.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSelectDestination: (Place) -> Unit,
    onMic: () -> Unit,
    onStart: () -> Unit,
    onRemoveWaypoints: () -> Unit,
    modifier: Modifier = Modifier,
    onSimulate: (String) -> Unit = {},
) = BoxWithConstraints(modifier.fillMaxSize()) {
    val dimens = FirstRoad.dimens
    // 낮은 화면(예: 1024×768)에서는 경로가 나오면 질문 제목을 빼고 경로 카드를 줄여, 카드가 한눈에 들어오게 한다.
    val isShort = maxHeight < SHORT_SCREEN_HEIGHT
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimens.screenPadding, vertical = dimens.gap),
        verticalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        val scroll = rememberScrollState()
        // 새 대답이 오면 아래로 내려 보여 준다 (음성으로만 조작할 때도 결과가 눈에 들어오게).
        LaunchedEffect(state.answer, state.heardText) { scroll.animateScrollTo(scroll.maxValue) }
        Column(
            Modifier.weight(1f).verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(dimens.gap),
        ) {
            if (!(isShort && state.plan != null)) {
                Text(stringResource(R.string.home_where), style = MaterialTheme.typography.headlineMedium)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                items(state.favorites, key = { it.id }) { place ->
                    BigActionButton(
                        text = place.name,
                        onClick = { onSelectDestination(place) },
                        isPrimary = place.id == state.selected?.id,
                    )
                }
            }
            when {
                state.isBuilding -> Text(stringResource(R.string.home_building), style = MaterialTheme.typography.bodyLarge)
                state.plan != null -> {
                    RoutePlanCard(plan = state.plan, fuelPercent = state.fuelPercent, batteryPercent = state.batteryPercent, compact = isShort)
                    if (state.plan.waypoints.isNotEmpty()) {
                        BigActionButton(stringResource(R.string.home_remove_waypoints), onRemoveWaypoints, isPrimary = false)
                    }
                    if (state.isStartFailed) {
                        Text(stringResource(R.string.home_start_failed), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> Text(
                    stringResource(R.string.home_pick_destination),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VoiceEcho(heard = state.heardText, answer = state.answer)
        }
        if (state.isDemo || state.isVoiceUnavailable) {
            VoiceSimulationPanel(stringArrayResource(R.array.home_voice_samples).toList(), onSimulate, singleRow = true)
        } else {
            VoiceHint(stringResource(R.string.voice_hint_home))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            BigActionButton(
                text = stringResource(if (state.isListening) R.string.voice_listening else R.string.voice_speak),
                onClick = onMic,
                modifier = Modifier.weight(1f),
                isPrimary = state.plan == null,
            )
            if (state.plan != null) {
                BigActionButton(stringResource(R.string.home_start), onStart, Modifier.weight(1f))
            }
        }
    }
}

private val SHORT_SCREEN_HEIGHT = 760.dp
