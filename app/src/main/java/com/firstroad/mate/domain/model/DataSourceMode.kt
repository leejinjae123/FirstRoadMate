package com.firstroad.mate.domain.model

/** 차량·내비 데이터를 어디서 가져올지. */
enum class DataSourceMode {
    /** Pleos 서비스가 있으면 실차, 없으면 데모. */
    AUTO,
    VEHICLE,
    DEMO,
}
