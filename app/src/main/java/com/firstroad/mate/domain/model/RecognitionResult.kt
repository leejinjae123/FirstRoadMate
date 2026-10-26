package com.firstroad.mate.domain.model

/** 음성 인식 한 번의 결과. */
sealed interface RecognitionResult {
    data class Text(val value: String) : RecognitionResult

    /** 음성 인식을 쓸 수 없는 환경(일반 에뮬레이터 등). 화면의 음성 시뮬레이션 버튼으로 대신한다. */
    data object Unavailable : RecognitionResult

    data object NoMatch : RecognitionResult
}
