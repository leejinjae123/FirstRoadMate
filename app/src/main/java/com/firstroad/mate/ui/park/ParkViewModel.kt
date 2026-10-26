package com.firstroad.mate.ui.park

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.firstroad.mate.domain.model.CheckType
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.LevelTier
import com.firstroad.mate.domain.model.NavigationState
import com.firstroad.mate.domain.model.ParkingCheckItem
import com.firstroad.mate.domain.model.ParkingRecord
import com.firstroad.mate.domain.model.PendingAction
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.model.VehicleSnapshot
import com.firstroad.mate.domain.model.VoiceCommand
import com.firstroad.mate.domain.model.VoiceContext
import com.firstroad.mate.domain.model.VoiceScreen
import com.firstroad.mate.domain.repository.DriveRecordRepository
import com.firstroad.mate.domain.repository.LocationRepository
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import com.firstroad.mate.domain.usecase.CalculateLevelUseCase
import com.firstroad.mate.domain.usecase.ExecuteVoiceCommandUseCase
import com.firstroad.mate.domain.usecase.ParkingCheckUseCase
import com.firstroad.mate.domain.usecase.ParseVoiceCommandUseCase
import com.firstroad.mate.domain.usecase.ResolvePendingActionUseCase
import com.firstroad.mate.ui.common.BaseViewModel
import com.firstroad.mate.ui.common.GuidanceSpeaker
import com.firstroad.mate.ui.common.SpeechDelivery
import com.firstroad.mate.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/** D. ⑥ 하차 안심 체크(시동 끄기 전 점검) + 주행 성장 리포트. */
@HiltViewModel
class ParkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsRepository: SettingsRepository,
    private val vehicleRepository: VehicleRepository,
    private val locationRepository: LocationRepository,
    private val navigationRepository: NavigationRepository,
    private val driveRecordRepository: DriveRecordRepository,
    private val parkingCheck: ParkingCheckUseCase,
    private val calculateLevel: CalculateLevelUseCase,
    private val parseVoiceCommand: ParseVoiceCommandUseCase,
    private val executeVoiceCommand: ExecuteVoiceCommandUseCase,
    private val resolvePendingAction: ResolvePendingActionUseCase,
    private val speaker: GuidanceSpeaker,
) : BaseViewModel() {

    private val destinationName = savedStateHandle.get<String>(AppDestinations.ARG_NAME).orEmpty()
    private val destination: GeoPoint? = AppDestinations.parsePoint(
        savedStateHandle.get<String>(AppDestinations.ARG_LAT),
        savedStateHandle.get<String>(AppDestinations.ARG_LNG),
    )
    private val sessionStartedAtMillis = System.currentTimeMillis()

    private val _uiState = MutableStateFlow(ParkUiState(destinationName = destinationName))
    val uiState: StateFlow<ParkUiState> = _uiState.asStateFlow()

    private var latestVehicle = VehicleSnapshot()
    private var pendingAction: PendingAction? = null

    /** 창문 질문에 답한 뒤 이어서 물을 공조 조절 동의 질문이 있으면 true. */
    private var askConsentNext = false

    init {
        speaker.attach(viewModelScope)
        vehicleRepository.vehicleSnapshot()
            .onEach { vehicle ->
                latestVehicle = vehicle
                _uiState.update { it.copy(checks = parkingCheck(vehicle)) }
            }
            .launchIn(viewModelScope)
        // 이번 주차 기록의 메모만 보여 준다 (지난 주차 메모가 보이지 않게).
        driveRecordRepository.latestParking()
            .onEach { parking ->
                val memo = parking?.takeIf { it.savedAtMillis >= sessionStartedAtMillis }?.memo
                _uiState.update { it.copy(parkingMemo = memo) }
            }
            .launchIn(viewModelScope)
        settingsRepository.isDemoActive()
            .onEach { isDemo -> _uiState.update { it.copy(isDemo = isDemo) } }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            saveParking()
            loadReport()
            announce(awaitKnownChecks())
            prepareCabinConsentQuestion()
        }
    }

    /**
     * 공조 자동 조절 사전 동의(opt-in): 이번 주행에 긴장 구간이 있었고 아직 묻지 않았으면, 정차한 지금 한 번만 묻는다.
     * 창문 질문이 먼저 떠 있으면 그 대답 뒤에 묻는다. 데모는 동의한 것으로 시연하므로 묻지 않는다.
     */
    private suspend fun prepareCabinConsentQuestion() {
        val hadTension = (_uiState.value.stats?.difficultPassed ?: 0) > 0
        if (!hadTension || settingsRepository.isDemoActive().first()) return
        if (settingsRepository.cabinAdjustConsent().first() != null) return
        if (pendingAction == null) askCabinConsent() else askConsentNext = true
    }

    private fun askCabinConsent() = ask(Guidance.AskCabinConsent, PendingAction.AllowCabinAdjust)

    /** 위치를 못 얻으면 목적지 좌표로 저장해, 음성 메모가 항상 이번 주차에 붙게 한다. */
    private suspend fun saveParking() {
        val location = locationRepository.lastKnownLocation() ?: destination ?: return
        driveRecordRepository.saveParking(
            ParkingRecord(location, destinationName, memo = null, savedAtMillis = System.currentTimeMillis()),
        )
        _uiState.update { it.copy(isParkingSaved = true) }
    }

    private suspend fun loadReport() {
        val records = driveRecordRepository.driveRecords().first()
        val now = calculateLevel(records)
        val before = calculateLevel(records.drop(1))
        val next = LevelTier.entries.getOrNull(now.tier.ordinal + 1)
        _uiState.update {
            it.copy(
                stats = records.firstOrNull()?.stats,
                level = now.tier.level,
                isLevelUp = records.isNotEmpty() && now.tier.level > before.tier.level,
                drivesToNextLevel = next?.let { tier -> (tier.requiredDrives - now.totalDrives).coerceAtLeast(1) },
            )
        }
    }

    /** 차량 값이 하나라도 읽힐 때까지 잠깐 기다린다. 첫 스냅숏은 아직 읽지 못한 항목이 비어 있다. */
    private suspend fun awaitKnownChecks(): List<ParkingCheckItem> =
        withTimeoutOrNull(KNOWN_CHECK_TIMEOUT_MILLIS) {
            vehicleRepository.vehicleSnapshot().first { parkingCheck(it).isNotEmpty() }
        }?.let(parkingCheck::invoke) ?: _uiState.value.checks

    /** 문제가 있는 항목만 묻는다. 없으면 "모두 괜찮아요"와 레벨 소식을 이어서 말한다. */
    private fun announce(checks: List<ParkingCheckItem>) {
        _uiState.update { it.copy(checks = checks, isChecking = false) }
        val issue = checks.firstOrNull { !it.isOk }
        if (issue == null) {
            if (checks.isNotEmpty()) say(Guidance.AllClear)
        } else {
            val guidance = Guidance.CheckIssue(issue.type, issue.area)
            val action = issue.toPendingAction()
            if (action == null) say(guidance) else ask(guidance, action)
        }
        if (_uiState.value.isLevelUp) speaker.say(Guidance.LevelUp(_uiState.value.level), delivery = SpeechDelivery.QUEUE)
    }

    fun onFix(item: ParkingCheckItem) = launchOnce(KEY_ANSWER) {
        pendingAction = item.toPendingAction() ?: return@launchOnce
        resolvePending(accepted = true)
    }

    fun onAnswer(accepted: Boolean) = launchOnce(KEY_ANSWER) { resolvePending(accepted) }

    fun onMicClick() = startListening()

    fun onSimulatedUtterance(text: String) = launchOnce(KEY_UTTERANCE) { handleUtterance(text) }

    /** "확인했어요" 또는 뒤로 가기: 경로를 정리하고 홈으로 돌아간다. */
    fun onDone() = launchOnce(KEY_DONE) {
        navigationRepository.cancelRoute()
        speaker.stop()
        _uiState.update { it.copy(isDone = true) }
    }

    private fun startListening() = launchOnce(KEY_LISTEN) { listen() }

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
            RecognitionResult.NoMatch -> if (pendingAction == null) say(Guidance.NotUnderstood)
        }
    }

    private suspend fun handleUtterance(text: String) {
        _uiState.update { it.copy(heardText = text) }
        val command = parseVoiceCommand(text)
        if (pendingAction != null && (command == VoiceCommand.Yes || command == VoiceCommand.No)) {
            resolvePending(command == VoiceCommand.Yes)
            return
        }
        when (command) {
            // 화면 버튼과 같은 일: 창문 '닫기', '확인했어요'.
            VoiceCommand.CloseWindow -> closeWindowByVoice()
            VoiceCommand.Finish -> onDone()
            VoiceCommand.Later -> if (pendingAction != null) resolvePending(accepted = false) else say(Guidance.Dismissed)
            else -> say(executeVoiceCommand(command, voiceContext()))
        }
    }

    private suspend fun closeWindowByVoice() {
        val openWindows = _uiState.value.checks.filter { it.type == CheckType.WINDOW && !it.isOk }
        val action = openWindows.firstNotNullOfOrNull { it.toPendingAction() }
        when {
            action != null -> {
                pendingAction = action
                resolvePending(accepted = true)
            }
            openWindows.isNotEmpty() -> say(Guidance.ControlNotPermitted)
            else -> say(Guidance.NoOpenWindow)
        }
    }

    private suspend fun resolvePending(accepted: Boolean) {
        val action = pendingAction ?: return
        pendingAction = null
        _uiState.update { it.copy(question = null) }
        say(resolvePendingAction(action, accepted, voiceContext()))
        if (askConsentNext) {
            askConsentNext = false
            askCabinConsent()
        }
    }

    private fun ask(guidance: Guidance, action: PendingAction) {
        pendingAction = action
        val text = speaker.say(guidance, delivery = SpeechDelivery.QUEUE) {
            if (pendingAction == action) startListening()
        }
        _uiState.update { it.copy(question = text) }
    }

    private fun say(guidance: Guidance) {
        val text = speaker.say(guidance)
        _uiState.update { it.copy(headline = text) }
    }

    private suspend fun voiceContext() = VoiceContext(
        navigation = NavigationState(),
        vehicle = latestVehicle,
        location = locationRepository.lastKnownLocation() ?: destination,
        destination = destination,
        lastGuidance = speaker.lastGuidance,
        nowMillis = System.currentTimeMillis(),
        screen = VoiceScreen.PARK,
    )

    private fun ParkingCheckItem.toPendingAction(): PendingAction? = when {
        isOk || !canFixRemotely -> null
        type == CheckType.WINDOW && area != null -> PendingAction.CloseWindow(area)
        else -> null
    }

    override fun onCleared() {
        speaker.stop()
        super.onCleared()
    }

    private companion object {
        const val KEY_ANSWER = "answer"
        const val KEY_LISTEN = "listen"
        const val KEY_UTTERANCE = "utterance"
        const val KEY_DONE = "done"
        const val KNOWN_CHECK_TIMEOUT_MILLIS = 3_000L
    }
}
