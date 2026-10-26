package com.firstroad.mate.ui.park

import com.firstroad.mate.domain.model.ParkingCheckItem

/** 하차 체크 화면 이벤트 묶음. */
data class ParkActions(
    val onFix: (ParkingCheckItem) -> Unit,
    val onAnswer: (Boolean) -> Unit,
    val onMic: () -> Unit,
    val onSimulate: (String) -> Unit,
    val onDone: () -> Unit,
)
