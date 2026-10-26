package com.firstroad.mate.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.firstroad.mate.data.local.AppDatabase
import com.firstroad.mate.data.local.dao.DifficultSpotDao
import com.firstroad.mate.data.local.dao.DriveRecordDao
import com.firstroad.mate.data.local.dao.ParkingRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun provideDriveRecordDao(database: AppDatabase): DriveRecordDao = database.driveRecordDao()

    @Provides
    fun provideParkingRecordDao(database: AppDatabase): ParkingRecordDao = database.parkingRecordDao()

    @Provides
    fun provideDifficultSpotDao(database: AppDatabase): DifficultSpotDao = database.difficultSpotDao()

    @Provides @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(SETTINGS_FILE) }

    private const val SETTINGS_FILE = "settings"
}
