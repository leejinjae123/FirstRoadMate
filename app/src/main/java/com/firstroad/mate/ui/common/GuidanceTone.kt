package com.firstroad.mate.ui.common

import com.firstroad.mate.domain.model.Guidance
import com.firstroad.mate.domain.model.WarningLevel

/** 안내 종류 → 화면 신호 색. */
internal fun Guidance.toTone(): Tone = when (this) {
    is Guidance.SignalReminder,
    is Guidance.SpeedOver -> Tone.ACTION

    is Guidance.WarningNotice -> level.toTone()
    is Guidance.ContinueAnswer -> level?.toTone() ?: Tone.OK

    is Guidance.PrepareLane,
    is Guidance.AskAddWaypoint,
    is Guidance.CheckIssue,
    is Guidance.RouteBriefing,
    is Guidance.NextTurnAnswer,
    Guidance.HardBrakeTip,
    is Guidance.CalmModeOn -> Tone.PREPARE

    else -> Tone.OK
}

internal fun WarningLevel.toTone(): Tone = when (this) {
    WarningLevel.STOP_NOW -> Tone.ACTION
    WarningLevel.CHECK_TODAY -> Tone.PREPARE
    WarningLevel.INFO -> Tone.OK
}
