package com.firstroad.mate.domain.model

/** 초보 졸업 구조 (기획서 6.4). requiredDrives는 테스트로 조정할 초기값이다. */
enum class LevelTier(val level: Int, val requiredDrives: Int) {
    SPROUT(1, 0),
    TRAINEE(2, 3),
    ADAPTING(3, 10),
    ALMOST(4, 25),
    GRADUATE(5, 50),
}
