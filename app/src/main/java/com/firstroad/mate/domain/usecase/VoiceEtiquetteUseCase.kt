package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.GuidancePriority
import javax.inject.Inject

/**
 * 음성 에티켓 (기획서 4.4): 지금 이 안내를 말해도 되는지 판단한다.
 * - 운전자 질문에 대한 대답·경고·회전 안내는 항상 말한다.
 * - 회전 4초 전부터는 회전 안내 외에는 말하지 않는다.
 * - 그 밖의 안내는 직전 안내와 8초 이상 떨어뜨린다.
 */
class VoiceEtiquetteUseCase @Inject constructor() {

    operator fun invoke(
        candidate: Guidance,
        nowMillis: Long,
        lastSpokenAtMillis: Long?,
        secondsToNextTurn: Float?,
    ): Boolean {
        if (candidate.priority.rank >= GuidancePriority.ANSWER.rank) return true
        if (secondsToNextTurn != null && secondsToNextTurn <= QUIET_BEFORE_TURN_SECONDS) return false
        if (lastSpokenAtMillis != null && nowMillis - lastSpokenAtMillis < MIN_GAP_MILLIS) return false
        return true
    }

    companion object {
        const val MIN_GAP_MILLIS = 8_000L
        const val QUIET_BEFORE_TURN_SECONDS = 4f
    }
}
