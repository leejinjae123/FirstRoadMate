package com.firstroad.mate.domain.model

data class DriveTick(
    val state: DriveSessionState,
    val events: List<DriveEvent>,
)
