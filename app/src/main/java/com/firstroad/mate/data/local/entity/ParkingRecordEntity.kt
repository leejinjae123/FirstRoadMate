package com.firstroad.mate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_record")
data class ParkingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val placeName: String,
    val memo: String?,
    val savedAtMillis: Long,
)
