package com.firstroad.mate.ui.common

/** 화면 상태 신호: 🟢 괜찮음 · 🟡 준비 · 🔴 지금 행동 (기획서 6.1). */
enum class Tone(val symbol: String) {
    OK("●"),
    PREPARE("▲"),
    ACTION("■"),
}
