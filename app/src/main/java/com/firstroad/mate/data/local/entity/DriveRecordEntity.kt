package com.firstroad.mate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive_record")
data class DriveRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val destinationName: String,
    val signalUsed: Int,
    val signalMissed: Int,
    val hardBrakes: Int,
    val difficultPassed: Int,
    val distanceMeters: Int,
    val durationSeconds: Int,
)
