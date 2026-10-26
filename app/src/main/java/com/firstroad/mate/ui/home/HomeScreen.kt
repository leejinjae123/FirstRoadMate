package com.firstroad.mate.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.ui.common.BeginnerScaffold
import com.firstroad.mate.ui.common.BigActionButton
import com.firstroad.mate.ui.common.DemoBadge
import com.firstroad.mate.ui.common.LevelBadge
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
            onVoiceSearch = viewModel::onVoiceSearch,
            onStart = viewModel::onStart,
            onRemoveWaypoints = viewModel::onRemoveWaypoints,
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

@Composable
fun HomeScreen(
    state: HomeUiState,
    onSelectDestination: (Place) -> Unit,
    onVoiceSearch: () -> Unit,
    onStart: () -> Unit,
    onRemoveWaypoints: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = FirstRoad.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.screenPadding, vertical = dimens.gap),
        verticalArrangement = Arrangement.spacedBy(dimens.gap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
            Text(stringResource(R.string.home_where), style = MaterialTheme.typography.headlineMedium)
            BigActionButton(
                text = stringResource(if (state.isListening) R.string.voice_listening else R.string.voice_speak),
                onClick = onVoiceSearch,
                isPrimary = false,
            )
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
                RoutePlanCard(plan = state.plan, fuelPercent = state.fuelPercent, batteryPercent = state.batteryPercent)
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap)) {
                    BigActionButton(stringResource(R.string.home_start), onStart, Modifier.weight(2f))
                    if (state.plan.waypoints.isNotEmpty()) {
                        BigActionButton(stringResource(R.string.home_remove_waypoints), onRemoveWaypoints, Modifier.weight(1f), isPrimary = false)
                    }
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
    }
}
