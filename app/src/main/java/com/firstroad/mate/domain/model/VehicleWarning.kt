package com.firstroad.mate.domain.model

data class VehicleWarning(
    val type: WarningType,
    val area: CabinArea? = null,
)
