package com.firstroad.mate.ui.home

import com.firstroad.mate.domain.model.Place
import com.firstroad.mate.domain.model.RoutePlan

data class HomeUiState(
    val level: Int = 1,
    val favorites: List<Place> = emptyList(),
    val selected: Place? = null,
    val plan: RoutePlan? = null,
    val fuelPercent: Int? = null,
    val batteryPercent: Int? = null,
    val isBuilding: Boolean = false,
    val isListening: Boolean = false,
    val isDemo: Boolean = false,
    val isVoiceUnavailable: Boolean = false,
    /** 마지막으로 들은 말과 앱의 대답. 음성으로만 조작할 때도 화면에서 확인할 수 있게 보여 준다. */
    val heardText: String? = null,
    val answer: String? = null,
    /** 경로 요청이 실패하면 true. 화면에 다시 시도 안내를 보여 준다 (중요한 결과는 UiState로). */
    val isStartFailed: Boolean = false,
    /** 경로 요청이 접수되면 채워진다. 화면은 이 값을 보고 주행 화면으로 이동한다. */
    val startedTrip: Place? = null,
)
