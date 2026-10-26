package com.firstroad.mate.ui.drive

import com.firstroad.mate.domain.model.TurnDirection
import com.firstroad.mate.domain.model.TurnGuide
import com.firstroad.mate.ui.common.Tone

data class DriveUiState(
    val destinationName: String = "",
    val isStarted: Boolean = false,
    val tone: Tone = Tone.OK,
    /** 다음 행동 카드의 핵심 문장 (마지막 안내). */
    val headline: String? = null,
    /** "1.8km 앞에서 오른쪽이에요" */
    val nextTurnText: String? = null,
    /** 계기판식 표시용: 다음 회전 방향 화살표와 거리 숫자. */
    val nextTurnDirection: TurnDirection? = null,
    val nextTurnMeters: Int? = null,
    val speedLimitKmh: Int? = null,
    /** 다음 회전 이후의 갈림길 (③ 리허설: 이어서 무엇이 오는지 미리). */
    val upcomingTurns: List<TurnGuide> = emptyList(),
    val remainingMinutes: Int? = null,
    val remainingMeters: Int? = null,
    val speedKmh: Int? = null,
    val tension: Int = 0,
    val isCalmActive: Boolean = false,
    val warning: WarningCardUi? = null,
    /** 확인 질문 (예/아니오 버튼과 함께 표시). */
    val question: String? = null,
    val isListening: Boolean = false,
    val heardText: String? = null,
    val isVoiceUnavailable: Boolean = false,
    val isRestricted: Boolean = false,
    val isDemo: Boolean = false,
    /** 도착 후 P단: 화면은 이 값을 보고 하차 체크로 이동한다 (중요한 결과는 UiState로). */
    val isFinished: Boolean = false,
)
