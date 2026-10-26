package com.firstroad.mate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.firstroad.mate.data.local.entity.ParkingRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingRecordDao {
    @Query("SELECT * FROM parking_record ORDER BY savedAtMillis DESC LIMIT 1")
    fun observeLatest(): Flow<ParkingRecordEntity?>

    @Insert
    suspend fun insert(record: ParkingRecordEntity)

    @Query("UPDATE parking_record SET memo = :memo WHERE id = (SELECT id FROM parking_record ORDER BY savedAtMillis DESC LIMIT 1)")
    suspend fun updateLatestMemo(memo: String)
}
