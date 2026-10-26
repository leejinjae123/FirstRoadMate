package com.firstroad.mate.domain.model

enum class TurnDirection {
    LEFT,
    RIGHT,
    U_TURN,
    STRAIGHT;

    /** 이 방향으로 돌 때 켜야 하는 방향지시등. 직진이면 null. */
    fun requiredSignal(): TurnSignal? = when (this) {
        LEFT, U_TURN -> TurnSignal.LEFT
        RIGHT -> TurnSignal.RIGHT
        STRAIGHT -> null
    }
}
