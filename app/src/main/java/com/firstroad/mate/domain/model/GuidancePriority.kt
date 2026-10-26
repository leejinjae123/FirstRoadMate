package com.firstroad.mate.domain.model

/** 음성 에티켓의 우선순위. 값이 클수록 먼저, 끊고서라도 말한다. */
enum class GuidancePriority(val rank: Int) {
    PRAISE(0),
    COACH(1),
    QUESTION(2),
    ANSWER(3),
    WARNING(4),
    TURN(5),
}
