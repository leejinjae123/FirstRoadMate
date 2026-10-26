package com.firstroad.mate.domain.usecase

import com.firstroad.mate.domain.model.PlaceCategory
import com.firstroad.mate.domain.model.VoiceCommand
import javax.inject.Inject

/**
 * 인식된 발화를 음성 명령으로 해석한다 (기획서 4.2 카탈로그, 규칙 기반).
 * 확인 질문의 대답("응", "아니")은 짧은 발화일 때만 인정해 다른 명령과 섞이지 않게 한다.
 */
class ParseVoiceCommandUseCase @Inject constructor() {

    operator fun invoke(utterance: String): VoiceCommand {
        val text = normalize(utterance)
        if (text.isEmpty()) return VoiceCommand.Unknown(utterance)

        parkingMemo(utterance)?.let { return it }
        if (text.length <= SHORT_ANSWER_MAX_LENGTH) {
            if (YES_WORDS.any { text == it }) return VoiceCommand.Yes
            if (NO_WORDS.any { text.startsWith(it) }) return VoiceCommand.No
        }
        return when {
            text.containsAny("뭐라고말", "어떻게말", "도움말", "뭐라고하면", "뭘말", "할수있는") -> VoiceCommand.Help
            text.containsAny("내차어디", "차어디", "어디세웠", "어디주차") -> VoiceCommand.WhereIsMyCar
            text.containsAny("어려웠", "어려워", "무서웠", "힘들었") -> VoiceCommand.MarkDifficultSpot
            text.containsAny("긴장", "떨려", "무서워", "진정") -> VoiceCommand.CalmDown
            text.containsAny("계속가도", "가도돼", "가도되", "경고등", "이거뭐야") -> VoiceCommand.ExplainWarning
            text.containsAny("어디서꺾", "어디서돌", "다음에어디", "다음어디", "다음회전", "언제꺾") -> VoiceCommand.NextTurn
            text.containsAny("뭐라고", "다시말", "다시들", "한번더") -> VoiceCommand.Repeat
            text.contains("경유") && text.containsAny("빼", "삭제", "취소", "지워") -> VoiceCommand.RemoveWaypoint
            text.contains("창문") && text.containsAny("닫", "올려") -> VoiceCommand.CloseWindow
            text.contains("나중에") -> VoiceCommand.Later
            text.containsAny("출발", "안내시작", "길안내해") || text in START_WORDS -> VoiceCommand.Start
            text.containsAny("끝", "종료", "안내그만", "확인했", "다됐", "홈으로", "처음으로") -> VoiceCommand.Finish
            else -> categoryOf(text)?.let { VoiceCommand.AddWaypoint(it) } ?: VoiceCommand.Unknown(utterance)
        }
    }

    private fun parkingMemo(utterance: String): VoiceCommand? {
        val compact = utterance.replace(" ", "")
        val index = compact.indexOf(MEMO_TRIGGER)
        if (index < 0) return null
        val afterTrigger = utterance.substringAfter("메모", missingDelimiterValue = "")
            .trim()
            .trimStart(',', '.', ' ')
            .trim()
        return if (afterTrigger.isEmpty()) null else VoiceCommand.ParkingMemo(afterTrigger)
    }

    private fun categoryOf(text: String): PlaceCategory? = when {
        text.contains("주유소") || text.contains("기름") -> PlaceCategory.GAS_STATION
        text.contains("충전소") || text.contains("충전") -> PlaceCategory.EV_CHARGER
        text.contains("휴게소") || text.contains("쉬었다") -> PlaceCategory.REST_AREA
        text.contains("주차장") -> PlaceCategory.PARKING
        text.contains("정비소") || text.contains("카센터") -> PlaceCategory.REPAIR_SHOP
        else -> null
    }

    private fun normalize(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }

    private fun String.containsAny(vararg words: String): Boolean = words.any { contains(it) }

    private companion object {
        const val SHORT_ANSWER_MAX_LENGTH = 5
        const val MEMO_TRIGGER = "주차메모"
        val YES_WORDS = setOf("응", "네", "예", "그래", "좋아", "어", "웅", "ㅇㅇ", "오케이", "ok", "해줘", "넣어줘", "부탁해")
        val NO_WORDS = setOf("아니", "싫어", "됐어", "그만", "괜찮아", "노")
        val START_WORDS = setOf("가자", "가자고", "시작", "시작해", "고고")
    }
}
