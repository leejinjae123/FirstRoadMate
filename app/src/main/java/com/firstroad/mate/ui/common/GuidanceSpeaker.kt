package com.firstroad.mate.ui.common

import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.GuidancePriority
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.repository.PhraseRepository
import com.firstroad.mate.domain.repository.VoiceRepository
import com.firstroad.mate.domain.usecase.VoiceEtiquetteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 화면(ViewModel)의 음성 출력 담당. ViewModel이 자기 viewModelScope로 [attach]해서 쓴다.
 * - 음성 에티켓(VoiceEtiquetteUseCase)으로 말할지 정하고, 더 높은 우선순위가 오면 말하던 것을 끊는다.
 * - 확인 질문과 중요한 안내는 버리지 않고 대기열에 넣어 앞 안내가 끝난 뒤 말한다.
 * - 멈출 때는 이 화면이 시작한 발화만 멈춘다 (다른 화면의 안내를 끊지 않음).
 */
class GuidanceSpeaker @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val phraseRepository: PhraseRepository,
    private val voiceEtiquette: VoiceEtiquetteUseCase,
) {
    private lateinit var scope: CoroutineScope
    private var speechJob: Job? = null
    private var speakingPriority: GuidancePriority? = null
    private var lastSpokenAtMillis: Long? = null
    private val queue = ArrayDeque<Utterance>()

    /** 마지막으로 전한 안내 ("뭐라고?"에 다시 들려준다). */
    var lastGuidance: Guidance? = null
        private set

    fun attach(viewModelScope: CoroutineScope) {
        scope = viewModelScope
    }

    fun phrase(guidance: Guidance): String = phraseRepository.phrase(guidance)

    /**
     * 안내를 문장으로 만들어 돌려주고, 전달 방식에 따라 말한다.
     * @param afterSpeech 이 문장을 다 말한 뒤 할 일 (확인 질문 뒤 듣기 등). 말하기 작업 밖에서 시작해야 한다.
     */
    fun say(
        guidance: Guidance,
        secondsToNextTurn: Float? = null,
        delivery: SpeechDelivery = SpeechDelivery.NORMAL,
        afterSpeech: (() -> Unit)? = null,
    ): String {
        val text = phraseRepository.phrase(guidance)
        if (guidance !is Guidance.NothingToRepeat) lastGuidance = guidance
        val utterance = Utterance(text, guidance.priority, afterSpeech)
        val isBusy = speechJob?.isActive == true
        val isImportant = guidance.priority.rank >= GuidancePriority.ANSWER.rank

        when (delivery) {
            SpeechDelivery.FORCE -> start(utterance)
            SpeechDelivery.QUEUE -> if (isBusy) queue.addLast(utterance) else start(utterance)
            SpeechDelivery.NORMAL -> {
                if (!voiceEtiquette(guidance, System.currentTimeMillis(), lastSpokenAtMillis, secondsToNextTurn)) return text
                val activeRank = speakingPriority?.rank ?: Int.MIN_VALUE
                when {
                    !isBusy || guidance.priority.rank >= activeRank -> start(utterance)
                    isImportant -> queue.addLast(utterance)
                }
            }
        }
        return text
    }

    /** 이 화면이 말하던 것을 멈추고 한 번 듣는다. 말하기 작업 밖에서 호출해야 한다. */
    suspend fun listen(): RecognitionResult {
        stop()
        return voiceRepository.recognize()
    }

    /** 이 화면이 시작한 발화와 대기열만 멈춘다. */
    fun stop() {
        queue.clear()
        speechJob?.cancel()
        speechJob = null
        speakingPriority = null
    }

    private fun start(utterance: Utterance) {
        speechJob?.cancel()
        speakingPriority = utterance.priority
        lastSpokenAtMillis = System.currentTimeMillis()
        speechJob = scope.launch {
            voiceRepository.speak(utterance.text)
            speakingPriority = null
            utterance.afterSpeech?.invoke()
            playNext()
        }
    }

    private fun playNext() {
        val next = queue.removeFirstOrNull() ?: return
        speechJob = null
        start(next)
    }

    private data class Utterance(
        val text: String,
        val priority: GuidancePriority,
        val afterSpeech: (() -> Unit)?,
    )
}
