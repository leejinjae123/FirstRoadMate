package com.firstroad.mate.ui.park

import com.firstroad.mate.domain.model.DriveStats
import com.firstroad.mate.domain.model.ParkingCheckItem

data class ParkUiState(
    val destinationName: String = "",
    val checks: List<ParkingCheckItem> = emptyList(),
    val isChecking: Boolean = true,
    val isParkingSaved: Boolean = false,
    val parkingMemo: String? = null,
    val stats: DriveStats? = null,
    val level: Int = 1,
    val isLevelUp: Boolean = false,
    /** 다음 레벨까지 남은 주행 횟수 (졸업이면 null). */
    val drivesToNextLevel: Int? = null,
    val headline: String? = null,
    val question: String? = null,
    val isListening: Boolean = false,
    val heardText: String? = null,
    val isVoiceUnavailable: Boolean = false,
    val isDemo: Boolean = false,
    /** "확인했어요"를 누르면 홈으로 돌아간다 (AND-UI-01). */
    val isDone: Boolean = false,
)
