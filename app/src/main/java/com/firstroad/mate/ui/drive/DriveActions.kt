package com.firstroad.mate.ui.drive

/** 주행 화면 이벤트 묶음 (Composable 매개변수를 줄이려고 묶는다). */
data class DriveActions(
    val onMic: () -> Unit,
    val onRepeat: () -> Unit,
    val onAnswer: (Boolean) -> Unit,
    val onAddSuggestedStop: () -> Unit,
    val onDismissWarning: () -> Unit,
    val onSimulate: (String) -> Unit,
    val onEndDrive: () -> Unit,
)
