package com.firstroad.mate.di

import com.firstroad.mate.data.repository.DriveRecordRepositoryImpl
import com.firstroad.mate.data.repository.LocationRepositoryImpl
import com.firstroad.mate.data.repository.NavigationRepositoryImpl
import com.firstroad.mate.data.repository.PhraseRepositoryImpl
import com.firstroad.mate.data.repository.PlaceRepositoryImpl
import com.firstroad.mate.data.repository.SettingsRepositoryImpl
import com.firstroad.mate.data.repository.VehicleRepositoryImpl
import com.firstroad.mate.data.repository.VoiceRepositoryImpl
import com.firstroad.mate.domain.repository.DriveRecordRepository
import com.firstroad.mate.domain.repository.LocationRepository
import com.firstroad.mate.domain.repository.NavigationRepository
import com.firstroad.mate.domain.repository.PhraseRepository
import com.firstroad.mate.domain.repository.PlaceRepository
import com.firstroad.mate.domain.repository.SettingsRepository
import com.firstroad.mate.domain.repository.VehicleRepository
import com.firstroad.mate.domain.repository.VoiceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** domain Repository 인터페이스 ↔ data 구현 바인딩. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds @Singleton
    abstract fun bindNavigationRepository(impl: NavigationRepositoryImpl): NavigationRepository

    @Binds @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds @Singleton
    abstract fun bindVoiceRepository(impl: VoiceRepositoryImpl): VoiceRepository

    @Binds @Singleton
    abstract fun bindPlaceRepository(impl: PlaceRepositoryImpl): PlaceRepository

    @Binds @Singleton
    abstract fun bindDriveRecordRepository(impl: DriveRecordRepositoryImpl): DriveRecordRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindPhraseRepository(impl: PhraseRepositoryImpl): PhraseRepository
}
