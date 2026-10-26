package com.firstroad.mate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.firstroad.mate.data.local.entity.DriveRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveRecordDao {
    @Query("SELECT * FROM drive_record ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<DriveRecordEntity>>

    @Insert
    suspend fun insert(record: DriveRecordEntity)
}
