package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.VoiceCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 기획서 4.2 음성 명령 카탈로그 12종이 모두 해석되는지 확인한다. */
class ParseVoiceCommandUseCaseTest {

    private val parse = ParseVoiceCommandUseCase()

    @Test
    fun `카탈로그 명령을 해석한다`() {
        val cases = mapOf(
            "주유소 넣어 줘" to VoiceCommand.AddWaypoint(PlaceCategory.GAS_STATION),
            "충전소 넣어 줘" to VoiceCommand.AddWaypoint(PlaceCategory.EV_CHARGER),
            "휴게소 넣어 줘" to VoiceCommand.AddWaypoint(PlaceCategory.REST_AREA),
            "주차장 넣어 줘" to VoiceCommand.AddWaypoint(PlaceCategory.PARKING),
            "경유지 빼 줘" to VoiceCommand.RemoveWaypoint,
            "다음에 어디서 꺾어?" to VoiceCommand.NextTurn,
            "뭐라고?" to VoiceCommand.Repeat,
            "다시 말해 줘" to VoiceCommand.Repeat,
            "이거 계속 가도 돼?" to VoiceCommand.ExplainWarning,
            "나 긴장돼" to VoiceCommand.CalmDown,
            "여기 어려웠어" to VoiceCommand.MarkDifficultSpot,
            "내 차 어디 있어?" to VoiceCommand.WhereIsMyCar,
            "응" to VoiceCommand.Yes,
            "네" to VoiceCommand.Yes,
            "아니" to VoiceCommand.No,
            "아니요" to VoiceCommand.No,
        )
        cases.forEach { (utterance, expected) -> assertEquals(utterance, expected, parse(utterance)) }
    }

    @Test
    fun `주차 메모는 메모 내용을 꺼낸다`() {
        assertEquals(VoiceCommand.ParkingMemo("B2 C14"), parse("주차 메모, B2 C14"))
    }

    @Test
    fun `긴 문장 속 응은 대답으로 보지 않는다`() {
        assertTrue(parse("응 그런데 휴게소는 어디야") !is VoiceCommand.Yes)
    }

    @Test
    fun `모르는 말은 Unknown`() {
        assertTrue(parse("오늘 날씨 어때") is VoiceCommand.Unknown)
    }
}
