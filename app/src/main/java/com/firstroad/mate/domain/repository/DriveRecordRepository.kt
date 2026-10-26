package com.firstroad.mate.domain.repository

import com.firstroad.mate.domain.model.DifficultSpot
import com.firstroad.mate.domain.model.DriveRecord
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.ParkingRecord
import kotlinx.coroutines.flow.Flow

interface DriveRecordRepository {
    fun driveRecords(): Flow<List<DriveRecord>>

    suspend fun saveDriveRecord(record: DriveRecord)

    fun latestParking(): Flow<ParkingRecord?>

    suspend fun saveParking(record: ParkingRecord)

    suspend fun updateParkingMemo(memo: String)

    fun difficultSpots(): Flow<List<DifficultSpot>>

    suspend fun addDifficultSpot(location: GeoPoint, createdAtMillis: Long)
}
