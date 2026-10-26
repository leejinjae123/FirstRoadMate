package com.firstroad.mate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.firstroad.mate.data.local.entity.DifficultSpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DifficultSpotDao {
    @Query("SELECT * FROM difficult_spot ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<DifficultSpotEntity>>

    @Insert
    suspend fun insert(spot: DifficultSpotEntity)
}
