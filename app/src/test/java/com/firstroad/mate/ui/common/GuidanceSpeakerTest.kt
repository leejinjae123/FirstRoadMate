package com.firstroad.mate.ui.common

import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.RecognitionResult
import com.firstroad.mate.domain.model.WarningLevel
import com.firstroad.mate.domain.model.WarningType
import com.firstroad.mate.domain.repository.PhraseRepository
import com.firstroad.mate.domain.repository.VoiceRepository
import com.firstroad.mate.domain.usecase.VoiceEtiquetteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 교차 검증(코드 리뷰 지적 1·9·10·15): 실제로 시간이 걸리는 음성 출력에서
 * 질문 대기열, 질문 뒤 듣기, '다시 듣기' 강제 재생이 동작하는지 확인한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GuidanceSpeakerTest {

    private class FakeVoice : VoiceRepository {
        val spoken = mutableListOf<String>()
        var recognizeCalls = 0

        override suspend fun speak(text: String) {
            delay(SPEECH_MILLIS)
            spoken += text
        }

        override suspend fun recognize(): RecognitionResult {
            recognizeCalls++
            delay(100)
            return RecognitionResult.Text("응")
        }
    }

    private val phrases = object : PhraseRepository {
        override fun phrase(guidance: Guidance): String = guidance.toString()
    }

    private fun TestScope.speaker(voice: FakeVoice) =
        GuidanceSpeaker(voice, phrases, VoiceEtiquetteUseCase()).also { it.attach(this) }

    private val warning = Guidance.WarningNotice(WarningType.TIRE_PRESSURE_LOW, WarningLevel.CHECK_TODAY)
    private val question = Guidance.AskAddWaypoint(PlaceCategory.GAS_STATION)

    @Test
    fun `경고 직후의 확인 질문은 버려지지 않고 경고가 끝난 뒤 말한다`() = runTest {
        val voice = FakeVoice()
        val speaker = speaker(voice)
        speaker.say(warning)
        speaker.say(question, delivery = SpeechDelivery.QUEUE)
        advanceUntilIdle()
        assertEquals(listOf(warning.toString(), question.toString()), voice.spoken)
    }

    @Test
    fun `질문을 다 말한 뒤 시작한 듣기는 취소되지 않고 결과를 받는다`() = runTest {
        val voice = FakeVoice()
        val speaker = speaker(voice)
        var heard: RecognitionResult? = null
        speaker.say(question, delivery = SpeechDelivery.QUEUE) {
            // ViewModel처럼 말하기 작업 밖에서 듣기를 시작한다.
            launch { heard = speaker.listen() }
        }
        advanceUntilIdle()
        assertEquals(1, voice.recognizeCalls)
        assertEquals(RecognitionResult.Text("응"), heard)
    }

    @Test
    fun `다시 듣기는 8초 간격 규칙과 상관없이 바로 말한다`() = runTest {
        val voice = FakeVoice()
        val speaker = speaker(voice)
        speaker.say(Guidance.PassedWell)
        advanceUntilIdle()
        speaker.say(Guidance.PassedWell) // 같은 칭찬을 곧바로: 에티켓에 막힘
        advanceUntilIdle()
        speaker.say(speaker.lastGuidance!!, delivery = SpeechDelivery.FORCE)
        advanceUntilIdle()
        assertEquals(2, voice.spoken.size)
    }

    @Test
    fun `회전 안내를 말하는 중에 온 대답은 버리지 않고 이어서 말한다`() = runTest {
        val voice = FakeVoice()
        val speaker = speaker(voice)
        speaker.say(Guidance.Arrived)
        speaker.say(Guidance.SpotMarked)
        advanceUntilIdle()
        assertTrue(voice.spoken.contains(Guidance.SpotMarked.toString()))
    }

    private companion object {
        const val SPEECH_MILLIS = 2_000L
    }
}
