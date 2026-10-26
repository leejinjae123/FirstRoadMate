package com.firstroad.mate.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.RemoveCircleOutline
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
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.ui.common.BeginnerScaffold
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.Caption
import com.firstroad.mate.ui.common.CockpitHeader
import com.firstroad.mate.ui.common.LevelBadge
import com.firstroad.mate.ui.common.VoiceDock
import com.firstroad.mate.ui.common.VoiceEcho
import com.firstroad.mate.ui.common.VoiceSimulationPanel
import com.firstroad.mate.ui.home.component.CarWidget
import com.firstroad.mate.ui.home.component.DestinationTile
import com.firstroad.mate.ui.home.component.LevelWidget
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
        topBar = {
            CockpitHeader(title = stringResource(R.string.app_name), isDemo = state.isDemo) { LevelBadge(state.level) }
        },
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

/**
 * 와이어프레임 A. 차량 화면의 홈처럼 왼쪽은 고르는 곳(자주 가는 곳·추천 경로), 오른쪽은 위젯 열(내 차·초보 졸업까지),
 * 맨 아래는 늘 같은 자리의 음성 바(마이크 + 출발하기). 좁은 화면(예: 1024×768)에서는 위젯 열을 접는다.
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
            // 새 대답이 오면 아래로 내려 보여 준다 (음성으로만 조작할 때도 결과가 눈에 들어오게).
            LaunchedEffect(state.answer, state.heardText) { if (!isShort) scroll.animateScrollTo(scroll.maxValue) }
            Column(
                Modifier.weight(if (isWide) 1.7f else 1f).fillMaxHeight().verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(dimens.gap),
            ) {
                if (!(isShort && state.plan != null)) {
                    Caption(stringResource(R.string.home_destinations))
                    Text(stringResource(R.string.home_where), style = MaterialTheme.typography.headlineLarge)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(dimens.smallGap)) {
                    items(state.favorites, key = { it.id }) { place ->
                        DestinationTile(place, isSelected = place.id == state.selected?.id, onClick = { onSelectDestination(place) })
                    }
                }
                when {
                    state.isBuilding -> Text(stringResource(R.string.home_building), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.plan != null -> {
                        RoutePlanCard(
                            plan = state.plan,
                            fuelPercent = state.fuelPercent,
                            batteryPercent = state.batteryPercent,
                            compact = isShort,
                            onRemoveWaypoints = onRemoveWaypoints.takeIf { isShort },
                        )
                        if (!isShort && state.plan.waypoints.isNotEmpty()) {
                            BigActionButton(stringResource(R.string.home_remove_waypoints), onRemoveWaypoints, isPrimary = false, icon = Icons.Rounded.RemoveCircleOutline)
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
                // 낮은 화면에서는 들은 말·대답을 음성 바에서 보여 주고, 여기서는 넓은 화면에서만 크게 보여 준다.
                if (!isShort) VoiceEcho(heard = state.heardText, answer = state.answer)
            }
            if (isWide) {
                Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    CarWidget(state.fuelPercent, state.batteryPercent, lastParkingText(state))
                    LevelWidget(state.level, state.levelProgress, state.drivesToNextLevel)
                }
            }
        }
        if (state.isDemo || state.isVoiceUnavailable) {
            VoiceSimulationPanel(stringArrayResource(R.array.home_voice_samples).toList(), onSimulate, singleRow = true)
        }
        VoiceDock(
            isListening = state.isListening,
            onMic = onMic,
            hint = stringResource(R.string.voice_hint_home),
            heard = state.heardText.takeIf { isShort },
            answer = state.answer.takeIf { isShort },
        ) {
            if (state.plan != null) {
                BigActionButton(stringResource(R.string.home_start), onStart, icon = Icons.Rounded.Navigation)
            }
        }
    }
}

/** "판교 회사 · B2 C14 · 9시간 전" */
@Composable
private fun lastParkingText(state: HomeUiState): String? {
    val name = state.lastParkingName ?: return null
    val ago = state.lastParkedAtMillis?.let { savedAt ->
        val minutes = ((System.currentTimeMillis() - savedAt) / MILLIS_PER_MINUTE).coerceAtLeast(0)
        when {
            minutes < 1 -> stringResource(R.string.home_parked_now)
            minutes < MINUTES_PER_HOUR -> stringResource(R.string.home_parked_ago, stringResource(R.string.unit_minutes, minutes.toInt()))
            else -> stringResource(R.string.home_parked_ago, stringResource(R.string.unit_hours, (minutes / MINUTES_PER_HOUR).toInt()))
        }
    }
    return listOfNotNull(name, state.lastParkingMemo, ago).joinToString(" · ")
}

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
