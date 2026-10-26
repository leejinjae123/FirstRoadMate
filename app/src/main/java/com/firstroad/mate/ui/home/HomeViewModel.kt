package com.firstroad.mate.ui.home

import androidx.lifecycle.viewModelScope
import com.firstroad.mate.R
import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.repository.DriveRecordRepository
import com.firstroad.mate.domain.repository.LocationRepository
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.PlaceRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import com.firstroad.mate.domain.repository.VoiceRepository
import com.firstroad.mate.domain.usecase.BuildBeginnerRouteUseCase
import com.firstroad.mate.domain.usecase.CalculateLevelUseCase
import com.firstroad.mate.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/** A. 홈 / ① 초보 맞춤 경로 빌더. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val locationRepository: LocationRepository,
    private val vehicleRepository: VehicleRepository,
    private val navigationRepository: NavigationRepository,
    private val voiceRepository: VoiceRepository,
    private val buildBeginnerRoute: BuildBeginnerRouteUseCase,
    calculateLevel: CalculateLevelUseCase,
    driveRecordRepository: DriveRecordRepository,
    settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(favorites = placeRepository.favoriteDestinations()) }
        }
        driveRecordRepository.driveRecords()
            .map(calculateLevel::invoke)
            .onEach { level -> _uiState.update { it.copy(level = level.tier.level) } }
            .launchIn(viewModelScope)
        settingsRepository.isDemoActive()
            .onEach { isDemo -> _uiState.update { it.copy(isDemo = isDemo) } }
            .launchIn(viewModelScope)
    }

    fun onSelectDestination(place: Place) = launchOnce(KEY_BUILD) {
        _uiState.update { it.copy(selected = place, plan = null, isBuilding = true, isStartFailed = false) }
        // 실차 첫 스냅숏은 연료 값이 아직 비어 있을 수 있어, 잠깐 기다렸다가 없으면 있는 값으로 진행한다.
        val vehicle = withTimeoutOrNull(ENERGY_WAIT_MILLIS) {
            vehicleRepository.vehicleSnapshot().first { it.energyPercent != null }
        } ?: vehicleRepository.vehicleSnapshot().first()
        val origin = locationRepository.lastKnownLocation() ?: place.location
        val plan = buildBeginnerRoute(place, origin, vehicle)
        _uiState.update {
            it.copy(
                plan = plan,
                fuelPercent = vehicle.fuelPercent,
                batteryPercent = vehicle.evBatteryPercent,
                isBuilding = false,
            )
        }
    }

    fun onRemoveWaypoints() {
        _uiState.update { state -> state.copy(plan = state.plan?.copy(waypoints = emptyList())) }
    }

    fun onStart() = launchOnce(KEY_START) {
        val plan = _uiState.value.plan ?: return@launchOnce
        _uiState.update { it.copy(isStartFailed = false) }
        when (navigationRepository.startRoute(plan)) {
            NaviCommandResult.REQUESTED -> _uiState.update { it.copy(startedTrip = plan.destination) }
            else -> _uiState.update { it.copy(isStartFailed = true) }
        }
    }

    fun onTripStartHandled() {
        _uiState.update { it.copy(startedTrip = null) }
    }

    /** "말로 찾기": 자주 가는 목적지 이름과 맞으면 선택한다. */
    fun onVoiceSearch() = launchOnce(KEY_VOICE) {
        _uiState.update { it.copy(isListening = true) }
        val result = voiceRepository.recognize()
        _uiState.update { it.copy(isListening = false) }
        when (result) {
            is RecognitionResult.Text -> {
                val spoken = result.value.replace(" ", "")
                val match = _uiState.value.favorites.firstOrNull { spoken.contains(it.name.replace(" ", "")) }
                if (match != null) onSelectDestination(match) else showToast(R.string.g_not_understood)
            }
            RecognitionResult.Unavailable -> showToast(R.string.voice_unavailable)
            RecognitionResult.NoMatch -> showToast(R.string.g_not_understood)
        }
    }

    private companion object {
        const val KEY_BUILD = "build"
        const val KEY_START = "start"
        const val KEY_VOICE = "voice"
        const val ENERGY_WAIT_MILLIS = 2_000L
    }
}
