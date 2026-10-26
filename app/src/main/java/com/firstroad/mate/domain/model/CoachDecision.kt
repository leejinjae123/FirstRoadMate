package com.firstroad.mate.domain.model

/**
 * 코칭 판단 결과.
 *
 * @param key 같은 회전 지점에서 같은 판단을 두 번 하지 않도록 구분하는 키.
 * @param guidance 말할 안내. 기록만 하고 말하지 않을 때는 null.
 */
data class CoachDecision(
    val key: String,
    val guidance: Guidance?,
    val countsAsSignalUsed: Boolean = false,
    val countsAsSignalMissed: Boolean = false,
)
