package com.firstroad.mate.domain.model

data class DriveRecord(
    val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val destinationName: String,
    val stats: DriveStats,
)
