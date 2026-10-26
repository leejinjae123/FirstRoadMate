package com.firstroad.mate.data.repository

import com.firstroad.mate.data.local.dao.DifficultSpotDao
import com.firstroad.mate.data.local.dao.DriveRecordDao
import com.firstroad.mate.data.local.dao.ParkingRecordDao
import com.firstroad.mate.data.local.entity.DifficultSpotEntity
import com.firstroad.mate.data.local.entity.DriveRecordEntity
import com.firstroad.mate.data.local.entity.ParkingRecordEntity
import com.firstroad.mate.domain.model.DifficultSpot
import com.firstroad.mate.domain.model.DriveRecord
import com.firstroad.mate.domain.model.DriveStats
import com.firstroad.mate.domain.model.GeoPoint
import com.firstroad.mate.domain.model.ParkingRecord
import com.firstroad.mate.domain.repository.DriveRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DriveRecordRepositoryImpl @Inject constructor(
    private val driveRecordDao: DriveRecordDao,
    private val parkingRecordDao: ParkingRecordDao,
    private val difficultSpotDao: DifficultSpotDao,
) : DriveRecordRepository {

    override fun driveRecords(): Flow<List<DriveRecord>> =
        driveRecordDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveDriveRecord(record: DriveRecord) = driveRecordDao.insert(record.toEntity())

    override fun latestParking(): Flow<ParkingRecord?> = parkingRecordDao.observeLatest().map { it?.toDomain() }

    override suspend fun saveParking(record: ParkingRecord) = parkingRecordDao.insert(
        ParkingRecordEntity(
            latitude = record.location.latitude,
            longitude = record.location.longitude,
            placeName = record.placeName,
            memo = record.memo,
            savedAtMillis = record.savedAtMillis,
        ),
    )

    override suspend fun updateParkingMemo(memo: String) = parkingRecordDao.updateLatestMemo(memo)

    override fun difficultSpots(): Flow<List<DifficultSpot>> = difficultSpotDao.observeAll().map { list ->
        list.map { DifficultSpot(it.id, GeoPoint(it.latitude, it.longitude), it.createdAtMillis) }
    }

    override suspend fun addDifficultSpot(location: GeoPoint, createdAtMillis: Long) =
        difficultSpotDao.insert(DifficultSpotEntity(latitude = location.latitude, longitude = location.longitude, createdAtMillis = createdAtMillis))

    private fun DriveRecordEntity.toDomain() = DriveRecord(
        id = id,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        destinationName = destinationName,
        stats = DriveStats(signalUsed, signalMissed, hardBrakes, difficultPassed, distanceMeters, durationSeconds),
    )

    private fun DriveRecord.toEntity() = DriveRecordEntity(
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        destinationName = destinationName,
        signalUsed = stats.signalUsed,
        signalMissed = stats.signalMissed,
        hardBrakes = stats.hardBrakes,
        difficultPassed = stats.difficultPassed,
        distanceMeters = stats.distanceMeters,
        durationSeconds = stats.durationSeconds,
    )

    private fun ParkingRecordEntity.toDomain() = ParkingRecord(
        location = GeoPoint(latitude, longitude),
        placeName = placeName,
        memo = memo,
        savedAtMillis = savedAtMillis,
    )
}
