package com.firstroad.mate.ui.home

import androidx.lifecycle.viewModelScope
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.LevelTier
import com.firstroad.mate.domain.model.NaviCommandResult
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.model.RoutePlan
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.VoiceCommand
import com.firstroad.mate.domain.model.VoiceContext
import com.firstroad.mate.domain.model.VoiceScreen
import com.firstroad.mate.domain.repository.DriveRecordRepository
import com.firstroad.mate.domain.repository.LocationRepository
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.PlaceRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import com.firstroad.mate.domain.usecase.BuildBeginnerRouteUseCase
import com.firstroad.mate.domain.usecase.CalculateLevelUseCase
import com.firstroad.mate.domain.usecase.ExecuteVoiceCommandUseCase
import com.firstroad.mate.domain.usecase.ParseVoiceCommandUseCase
import com.firstroad.mate.ui.common.BaseViewModel
import com.firstroad.mate.ui.common.GuidanceSpeaker
import com.firstroad.mate.ui.common.SpeechDelivery
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

/**
 * A. 홈 / ① 초보 맞춤 경로 빌더. 버튼으로 하는 일은 모두 말로도 할 수 있다:
 * "회사 가자"(목적지) → "판교 회사까지 37분, 주유소 들러요. 출발할까요?" → "응"(출발), "경유지 빼 줘".
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val locationRepository: LocationRepository,
    private val vehicleRepository: VehicleRepository,
    private val navigationRepository: NavigationRepository,
    private val buildBeginnerRoute: BuildBeginnerRouteUseCase,
    private val parseVoiceCommand: ParseVoiceCommandUseCase,
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase,
    private val speaker: GuidanceSpeaker,
    calculateLevel: CalculateLevelUseCase,
    driveRecordRepository: DriveRecordRepository,
    settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** "출발할까요?"라고 물은 뒤 대답을 기다리는 중이면 true. */
    private var isAwaitingStart = false

    init {
        speaker.attach(viewModelScope)
        viewModelScope.launch {
            _uiState.update { it.copy(favorites = placeRepository.favoriteDestinations()) }
        }
        driveRecordRepository.driveRecords()
            .map(calculateLevel::invoke)
            .onEach { level ->
                val next = LevelTier.entries.getOrNull(level.tier.ordinal + 1)
                _uiState.update {
                    it.copy(
                        level = level.tier.level,
                        levelProgress = level.progress,
                        drivesToNextLevel = next?.let { tier -> (tier.requiredDrives - level.totalDrives).coerceAtLeast(1) },
                    )
                }
            }
            .launchIn(viewModelScope)
        // '내 차' 위젯: 출발 전에도 연료·배터리와 지난 주차 위치를 보여 준다.
        vehicleRepository.vehicleSnapshot()
            .onEach { vehicle -> _uiState.update { it.copy(fuelPercent = vehicle.fuelPercent ?: it.fuelPercent, batteryPercent = vehicle.evBatteryPercent ?: it.batteryPercent) } }
            .launchIn(viewModelScope)
        driveRecordRepository.latestParking()
            .onEach { parking ->
                _uiState.update { it.copy(lastParkingName = parking?.placeName, lastParkingMemo = parking?.memo, lastParkedAtMillis = parking?.savedAtMillis) }
            }
            .launchIn(viewModelScope)
        settingsRepository.isDemoActive()
            .onEach { isDemo -> _uiState.update { it.copy(isDemo = isDemo) } }
            .launchIn(viewModelScope)
    }

    fun onSelectDestination(place: Place) = launchOnce(KEY_BUILD) {
        isAwaitingStart = false
        _uiState.update { it.copy(selected = place, plan = null, isBuilding = true, isStartFailed = false, answer = null) }
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
        askToStart(plan)
    }

    fun onRemoveWaypoints() {
        _uiState.update { state -> state.copy(plan = state.plan?.copy(waypoints = emptyList())) }
    }

    fun onStart() = launchOnce(KEY_START) {
        val plan = _uiState.value.plan ?: return@launchOnce
        isAwaitingStart = false
        speaker.stop()
        cancelRunning(KEY_LISTEN)
        _uiState.update { it.copy(isStartFailed = false, isListening = false) }
        when (navigationRepository.startRoute(plan)) {
            NaviCommandResult.REQUESTED -> _uiState.update { it.copy(startedTrip = plan.destination) }
            else -> _uiState.update { it.copy(isStartFailed = true) }
        }
    }

    /** 주행 화면으로 넘어가면 홈을 처음 상태로 돌려, 다녀와서 지난 경로와 질문이 남아 있지 않게 한다. */
    fun onTripStartHandled() {
        _uiState.update {
            it.copy(startedTrip = null, selected = null, plan = null, heardText = null, answer = null, isStartFailed = false)
        }
    }

    fun onMicClick() = launchOnce(KEY_LISTEN) { listen() }

    fun onSimulatedUtterance(text: String) = launchOnce(KEY_UTTERANCE) { handleUtterance(text) }

    /** 경로 요약을 짧게 말하고 "출발할까요?"라고 묻는다. 다 말하면 대답을 듣는다. */
    private fun askToStart(plan: RoutePlan) {
        isAwaitingStart = true
        val guidance = Guidance.RouteReady(plan.destination.name, plan.estimatedMinutes, plan.waypoints.map { it.place.category })
        val text = speaker.say(guidance, delivery = SpeechDelivery.FORCE) {
            if (isAwaitingStart) onMicClick()
        }
        _uiState.update { it.copy(answer = text) }
    }

    private suspend fun listen() {
        _uiState.update { it.copy(isListening = true) }
        val result = try {
            speaker.listen()
        } finally {
            _uiState.update { it.copy(isListening = false) }
        }
        when (result) {
            is RecognitionResult.Text -> handleUtterance(result.value)
            RecognitionResult.Unavailable -> _uiState.update { it.copy(isVoiceUnavailable = true) }
            RecognitionResult.NoMatch -> if (!isAwaitingStart) say(Guidance.NotUnderstood)
        }
    }

    private suspend fun handleUtterance(text: String) {
        _uiState.update { it.copy(heardText = text) }
        // "회사 가자"처럼 자주 가는 곳 이름이 들어 있으면 그곳으로 경로를 만든다.
        matchFavorite(text)?.let {
            onSelectDestination(it)
            return
        }
        val plan = _uiState.value.plan
        when (val command = parseVoiceCommand(text)) {
            VoiceCommand.Start, VoiceCommand.Yes ->
                if (plan != null) onStart() else say(Guidance.Help(VoiceScreen.HOME))
            VoiceCommand.No, VoiceCommand.Later -> {
                isAwaitingStart = false
                say(Guidance.Dismissed)
            }
            VoiceCommand.RemoveWaypoint ->
                if (plan?.waypoints.isNullOrEmpty()) {
                    say(Guidance.NoActiveRoute)
                } else {
                    onRemoveWaypoints()
                    say(Guidance.WaypointRemoved)
                }
            else -> say(executeVoiceCommand(command, voiceContext()))
        }
    }

    /** 이름의 낱말(두 글자 이상)이 가장 많이 들어 있는 즐겨찾기. "자주 가는"처럼 흔한 말은 세지 않는다. */
    private fun matchFavorite(text: String): Place? {
        val spoken = text.replace(" ", "")
        return _uiState.value.favorites
            .map { place ->
                val words = place.name.split(" ").filter { it.length >= 2 && it !in GENERIC_WORDS }
                place to words.count { spoken.contains(it) }
            }
            .filter { (_, matched) -> matched > 0 }
            .maxByOrNull { (_, matched) -> matched }
            ?.first
    }

    private fun say(guidance: Guidance) {
        val text = speaker.say(guidance, delivery = SpeechDelivery.FORCE)
        _uiState.update { it.copy(answer = text) }
    }

    private suspend fun voiceContext() = VoiceContext(
        navigation = NavigationState(),
        vehicle = withTimeoutOrNull(ENERGY_WAIT_MILLIS) { vehicleRepository.vehicleSnapshot().first() } ?: VehicleSnapshot(),
        location = locationRepository.lastKnownLocation(),
        destination = _uiState.value.selected?.location,
        lastGuidance = speaker.lastGuidance,
        nowMillis = System.currentTimeMillis(),
        screen = VoiceScreen.HOME,
    )

    override fun onCleared() {
        speaker.stop()
        super.onCleared()
    }

    private companion object {
        const val KEY_BUILD = "build"
        const val KEY_START = "start"
        const val KEY_LISTEN = "listen"
        const val KEY_UTTERANCE = "utterance"
        const val ENERGY_WAIT_MILLIS = 2_000L
        val GENERIC_WORDS = setOf("자주", "가는")
    }
}
