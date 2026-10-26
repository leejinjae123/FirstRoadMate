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

    /** 졸업: 코칭·칭찬·진정 모드를 멈추고 경고등 통역사·하차 체크·주차 메모만 남긴다. 습관 기록은 조용히 계속한다. */
    val isGraduated: Boolean get() = tier == LevelTier.GRADUATE

    companion object {
        val Initial = BeginnerLevel(LevelTier.SPROUT, totalDrives = 0, progress = 0f)
    }
}
