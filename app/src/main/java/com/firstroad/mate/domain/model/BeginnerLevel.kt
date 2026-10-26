package com.firstroad.mate.domain.model

/**
 * @param progress 다음 레벨까지 진행률 0.0~1.0. 졸업이면 1.0.
 */
data class BeginnerLevel(
    val tier: LevelTier,
    val totalDrives: Int,
    val progress: Float,
) {
    /** 레벨이 오를수록 코칭·칭찬 빈도를 줄인다 (음성 에티켓 4.4-4). */
    val coachesSignalPreparation: Boolean get() = tier.level <= LevelTier.TRAINEE.level
    val praisesOften: Boolean get() = tier.level <= LevelTier.ADAPTING.level

    companion object {
        val Initial = BeginnerLevel(LevelTier.SPROUT, totalDrives = 0, progress = 0f)
    }
}
