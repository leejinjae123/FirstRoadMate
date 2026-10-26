package com.firstroad.mate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.firstroad.mate.data.local.dao.DifficultSpotDao
import com.firstroad.mate.data.local.dao.DriveRecordDao
import com.firstroad.mate.data.local.dao.ParkingRecordDao
import com.firstroad.mate.data.local.entity.DifficultSpotEntity
import com.firstroad.mate.data.local.entity.DriveRecordEntity
import com.firstroad.mate.data.local.entity.ParkingRecordEntity

@Database(
    entities = [DriveRecordEntity::class, ParkingRecordEntity::class, DifficultSpotEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun driveRecordDao(): DriveRecordDao
    abstract fun parkingRecordDao(): ParkingRecordDao
    abstract fun difficultSpotDao(): DifficultSpotDao

    companion object {
        const val NAME = "firstroad.db"
    }
}
