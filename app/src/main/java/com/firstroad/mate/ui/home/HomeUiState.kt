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
    /** 경로 요청이 실패하면 true. 화면에 다시 시도 안내를 보여 준다 (AND-UI-01: 중요한 결과는 UiState). */
    val isStartFailed: Boolean = false,
    /** 경로 요청이 접수되면 채워진다. 화면은 이 값을 보고 주행 화면으로 이동한다 (AND-UI-01). */
    val startedTrip: Place? = null,
)
