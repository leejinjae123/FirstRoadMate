package com.firstroad.mate.ui.drive

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.firstroad.mate.domain.model.DriveEvent
import com.firstroad.mate.domain.model.DriveInput
import com.firstroad.mate.domain.model.DriveRecord
import com.firstroad.mate.domain.model.DriveSessionState
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.PendingAction
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.VoiceCommand
import com.firstroad.mate.domain.model.VoiceContext
import com.firstroad.mate.domain.repository.DriveRecordRepository
import com.firstroad.mate.domain.repository.LocationRepository
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import com.firstroad.mate.domain.usecase.CalculateLevelUseCase
import com.firstroad.mate.domain.usecase.DriveTickUseCase
import com.firstroad.mate.domain.usecase.ExecuteVoiceCommandUseCase
import com.firstroad.mate.domain.usecase.ParseVoiceCommandUseCase
import com.firstroad.mate.domain.usecase.ResolvePendingActionUseCase
import com.firstroad.mate.ui.common.BaseViewModel
import com.firstroad.mate.ui.common.GuidanceSpeaker
import com.firstroad.mate.ui.common.SpeechDelivery
import com.firstroad.mate.ui.common.toTone
import com.firstroad.mate.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * B·C. 주행 화면. 판단은 DriveTickUseCase(domain)가 하고, 이 ViewModel은
 * 그 결과 이벤트를 화면 상태·음성·Repository 호출로 옮기기만 한다.
 */
@HiltViewModel
class DriveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    navigationRepository: NavigationRepository,
    locationRepository: LocationRepository,
    settingsRepository: SettingsRepository,
    calculateLevel: CalculateLevelUseCase,
    private val vehicleRepository: VehicleRepository,
    private val driveRecordRepository: DriveRecordRepository,
    private val driveTick: DriveTickUseCase,
    private val parseVoiceCommand: ParseVoiceCommandUseCase,
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase,
    private val resolvePendingAction: ResolvePendingActionUseCase,
    private val speaker: GuidanceSpeaker,
) : BaseViewModel() {

    private val destinationName: String = savedStateHandle.get<String>(AppDestinations.ARG_NAME).orEmpty()
    private val destination: GeoPoint? = AppDestinations.parsePoint(
        savedStateHandle.get<String>(AppDestinations.ARG_LAT),
        savedStateHandle.get<String>(AppDestinations.ARG_LNG),
    )

    private val _uiState = MutableStateFlow(DriveUiState(destinationName = destinationName))
    val uiState: StateFlow<DriveUiState> = _uiState.asStateFlow()

    private var session = DriveSessionState()
    private var latestNavigation = NavigationState()
    private var latestVehicle = VehicleSnapshot()
    private var latestLocation: GeoPoint? = null
    private var pendingAction: PendingAction? = null
    private var isFinishing = false
    private var headlineGuidance: Guidance? = null

    init {
        speaker.attach(viewModelScope)
        combine(
            vehicleRepository.vehicleSnapshot(),
            navigationRepository.navigationState(),
            driveRecordRepository.driveRecords().map(calculateLevel::invoke),
            driveRecordRepository.difficultSpots(),
            locationRepository.locationUpdates().map<GeoPoint, GeoPoint?> { it }.onStart { emit(null) },
        ) { vehicle, navigation, level, spots, location ->
            DriveInput(vehicle, navigation, level, System.currentTimeMillis(), location, destination, spots)
        }.onEach(::onTick).launchIn(viewModelScope)

        vehicleRepository.isDrivingRestricted()
            .onEach { restricted -> _uiState.update { it.copy(isRestricted = restricted) } }
            .launchIn(viewModelScope)
        settingsRepository.isDemoActive()
            .onEach { isDemo -> _uiState.update { it.copy(isDemo = isDemo) } }
            .launchIn(viewModelScope)
    }

    private fun onTick(input: DriveInput) {
        latestNavigation = input.navigation
        latestVehicle = input.vehicle
        latestLocation = input.location
        val tick = driveTick(session, input)
        session = tick.state
        _uiState.update { state ->
            state.copy(
                isStarted = session.startedAtMillis != null,
                speedKmh = input.vehicle.speedKmh?.toInt(),
                tension = session.tension,
                isCalmActive = session.isCalmActive,
                remainingMeters = input.navigation.remainingMeters,
                nextTurnText = input.navigation.nextTurn?.let {
                    speaker.phrase(Guidance.NextTurnAnswer(it.direction, it.distanceMeters))
                },
                nextTurnDirection = input.navigation.nextTurn?.direction,
                nextTurnMeters = input.navigation.nextTurn?.distanceMeters,
                speedLimitKmh = input.navigation.speedLimitKmh,
                upcomingTurns = input.navigation.upcomingTurns.drop(1).take(UPCOMING_TURNS),
                remainingMinutes = input.navigation.remainingSeconds?.let { (it + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE },
            )
        }
        tick.events.forEach(::handleEvent)
        refreshHeadlineDistance(input.navigation.nextTurn)
    }

    private fun handleEvent(event: DriveEvent) {
        when (event) {
            is DriveEvent.Say -> say(event.guidance)
            is DriveEvent.Ask -> ask(event.guidance, event.action)
            is DriveEvent.ShowWarning -> _uiState.update {
                val explanation = event.explanation
                it.copy(
                    warning = WarningCardUi(
                        level = explanation.level,
                        message = speaker.phrase(Guidance.WarningNotice(explanation.warning.type, explanation.level)),
                        suggestedStop = explanation.suggestedStop,
                    ),
                )
            }
            DriveEvent.StartCalm -> viewModelScope.launch {
                say(executeVoiceCommand(VoiceCommand.CalmDown, voiceContext()))
            }
            DriveEvent.EndCalm -> viewModelScope.launch { vehicleRepository.restoreCabin() }
            DriveEvent.Finished -> finishDrive()
        }
    }

    fun onMicClick() = startListening()

    fun onSimulatedUtterance(text: String) = launchOnce(KEY_UTTERANCE) { handleUtterance(text) }

    fun onAnswer(accepted: Boolean) = launchOnce(KEY_ANSWER) { resolvePending(accepted) }

    /** "다시 듣기"는 운전자가 직접 요청한 것이라 에티켓 간격과 상관없이 바로 말한다. */
    fun onRepeat() {
        val guidance = speaker.lastGuidance ?: Guidance.NothingToRepeat
        val text = speaker.say(guidance, delivery = SpeechDelivery.FORCE)
        _uiState.update { it.copy(headline = text, tone = guidance.toTone()) }
    }

    fun onAddSuggestedStop() = launchOnce(KEY_ANSWER) {
        val stop = _uiState.value.warning?.suggestedStop ?: return@launchOnce
        pendingAction = PendingAction.AddWaypoint(stop)
        resolvePending(accepted = true)
    }

    fun onDismissWarning() {
        _uiState.update { it.copy(warning = null) }
    }

    fun onEndDrive() = finishDrive()

    private fun startListening() = launchOnce(KEY_LISTEN) { listen() }

    private suspend fun listen() {
        val asked = pendingAction
        val startedAt = System.currentTimeMillis()
        _uiState.update { it.copy(isListening = true) }
        val result = try {
            speaker.listen()
        } finally {
            _uiState.update { it.copy(isListening = false) }
        }
        when (result) {
            is RecognitionResult.Text -> handleUtterance(result.value)
            RecognitionResult.Unavailable -> _uiState.update { it.copy(isVoiceUnavailable = true) }
            RecognitionResult.NoMatch -> if (pendingAction == null) say(Guidance.NotUnderstood)
        }
        // 대답 시간(7초)이 지나도 대답이 없으면 질문을 거둔다. 주행 중 화면에 질문이 계속 남지 않게 하고,
        // 경고 카드의 버튼으로는 계속 고를 수 있다. 음성 인식이 없으면 바로 끝나므로 남은 시간만큼 기다린다.
        if (asked != null && pendingAction == asked) {
            val left = ANSWER_WINDOW_MILLIS - (System.currentTimeMillis() - startedAt)
            if (left > 0) delay(left)
            if (pendingAction == asked) {
                pendingAction = null
                _uiState.update { it.copy(question = null) }
            }
        }
    }

    private suspend fun handleUtterance(text: String) {
        _uiState.update { it.copy(heardText = text) }
        val command = parseVoiceCommand(text)
        if (pendingAction != null && (command == VoiceCommand.Yes || command == VoiceCommand.No)) {
            resolvePending(accepted = command == VoiceCommand.Yes)
            return
        }
        when (command) {
            // 화면 버튼과 같은 일: '주행 끝내기', 경고 카드 '나중에'.
            // '주행 끝내기' 버튼처럼 정차 중에만 끝낸다. 달리는 중에 하차 체크로 넘어가지 않게.
            VoiceCommand.Finish -> if (_uiState.value.isRestricted) say(Guidance.ParkFirst) else finishDrive()
            VoiceCommand.Later -> {
                if (pendingAction != null) resolvePending(accepted = false) else say(Guidance.Dismissed)
                onDismissWarning()
            }
            else -> say(executeVoiceCommand(command, voiceContext()))
        }
    }

    private suspend fun resolvePending(accepted: Boolean) {
        val action = pendingAction ?: return
        pendingAction = null
        // 경유지 질문에 대답했으면(응·아니) 경고 카드의 같은 버튼을 거둬 두 번 넣지 않게 한다.
        val answeredStop = action is PendingAction.AddWaypoint
        _uiState.update {
            it.copy(question = null, warning = if (answeredStop) it.warning?.copy(suggestedStop = null) else it.warning)
        }
        say(resolvePendingAction(action, accepted, voiceContext()))
    }

    /** 확인 질문: 앞 안내가 끝난 뒤 말하고, 다 말하면 대답을 듣는다. */
    private fun ask(guidance: Guidance, action: PendingAction) {
        pendingAction = action
        val text = speaker.say(guidance, delivery = SpeechDelivery.QUEUE) {
            if (pendingAction == action) startListening()
        }
        _uiState.update { it.copy(question = text) }
    }

    private fun say(guidance: Guidance) {
        val text = speaker.say(guidance, secondsToNextTurn())
        // 경고 문장은 경고 카드에만 보여 준다 (같은 문장을 두 번 보이지 않게, '하나씩' 원칙).
        if (guidance is Guidance.WarningNotice) return
        headlineGuidance = guidance
        _uiState.update { it.copy(headline = text, tone = guidance.toTone()) }
    }

    /**
     * 카드의 거리 숫자는 실시간인데 문장은 말한 순간의 거리라 "80 m" 옆에 "1km 앞"이 남는다.
     * 같은 회전에 대한 안내면 문장의 거리도 지금 거리로 바꿔 보여 준다 (말은 다시 하지 않는다).
     */
    private fun refreshHeadlineDistance(nextTurn: TurnGuide?) {
        if (nextTurn == null) return
        // 같은 회전인지: 방향이 같고 거리가 줄어드는 중이어야 한다 (거리가 늘면 다음 회전으로 바뀐 것).
        fun sameTurn(direction: TurnDirection, shownMeters: Int) =
            direction == nextTurn.direction && nextTurn.distanceMeters <= shownMeters + SAME_TURN_TOLERANCE_METERS
        val refreshed: Guidance = when (val shown = headlineGuidance) {
            is Guidance.PrepareLane -> shown.takeIf { sameTurn(it.direction, it.distanceMeters) }?.copy(distanceMeters = nextTurn.distanceMeters)
            is Guidance.NextTurnAnswer -> shown.takeIf { sameTurn(it.direction, it.distanceMeters) }?.copy(distanceMeters = nextTurn.distanceMeters)
            else -> null
        } ?: return
        headlineGuidance = refreshed
        val text = speaker.phrase(refreshed)
        if (text != _uiState.value.headline) _uiState.update { it.copy(headline = text) }
    }

    /** 도착 후 P단(자동) 또는 '주행 끝내기'(수동) 중 먼저 온 쪽에서 한 번만 저장한다. */
    private fun finishDrive() {
        if (isFinishing) return
        isFinishing = true
        launchOnce(KEY_FINISH) {
            val now = System.currentTimeMillis()
            val started = session.startedAtMillis ?: now
            val stats = session.stats.copy(durationSeconds = ((now - started) / MILLIS_PER_SECOND).toInt())
            driveRecordRepository.saveDriveRecord(
                DriveRecord(startedAtMillis = started, endedAtMillis = now, destinationName = destinationName, stats = stats),
            )
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    private fun voiceContext() = VoiceContext(
        navigation = latestNavigation,
        vehicle = latestVehicle,
        location = latestLocation,
        destination = destination,
        lastGuidance = speaker.lastGuidance,
        nowMillis = System.currentTimeMillis(),
    )

    private fun secondsToNextTurn(): Float? {
        val turn = latestNavigation.nextTurn ?: return null
        val speedMs = (latestVehicle.speedKmh ?: return null) / KMH_PER_MS
        return if (speedMs <= 0f) null else turn.distanceMeters / speedMs
    }

    override fun onCleared() {
        speaker.stop()
        super.onCleared()
    }

    private companion object {
        const val KEY_LISTEN = "listen"
        const val ANSWER_WINDOW_MILLIS = 7_000L
        const val UPCOMING_TURNS = 2
        const val SAME_TURN_TOLERANCE_METERS = 50
        const val SECONDS_PER_MINUTE = 60
        const val KEY_UTTERANCE = "utterance"
        const val KEY_ANSWER = "answer"
        const val KEY_FINISH = "finish"
        const val KMH_PER_MS = 3.6f
        const val MILLIS_PER_SECOND = 1_000L
    }
}
