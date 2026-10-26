package com.firstroad.mate.di

import com.firstroad.mate.data.datasource.DemoSource
import com.firstroad.mate.data.datasource.PlatformSource
import com.firstroad.mate.data.datasource.PleosSource
import com.firstroad.mate.data.datasource.voice.SpeechInputDataSource
import com.firstroad.mate.data.datasource.voice.SpeechOutputDataSource
import com.firstroad.mate.data.datasource.voice.android.AndroidSpeechInputDataSource
import com.firstroad.mate.data.datasource.voice.android.AndroidSpeechOutputDataSource
import com.firstroad.mate.data.datasource.voice.pleos.PleosSpeechInputDataSource
import com.firstroad.mate.data.datasource.voice.pleos.PleosSpeechOutputDataSource
import com.firstroad.mate.data.datasource.demo.DemoLocationDataSource
import com.firstroad.mate.data.datasource.demo.DemoNaviDataSource
import com.firstroad.mate.data.datasource.demo.DemoVehicleDataSource
import com.firstroad.mate.data.datasource.location.LocationDataSource
import com.firstroad.mate.data.datasource.location.pleos.PleosLocationDataSource
import com.firstroad.mate.data.datasource.navi.NaviDataSource
import com.firstroad.mate.data.datasource.navi.pleos.PleosNaviDataSource
import com.firstroad.mate.data.datasource.vehicle.VehicleDataSource
import com.firstroad.mate.data.datasource.vehicle.pleos.PleosVehicleDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** 실차(@PleosSource) / 데모(@DemoSource) 데이터 소스 바인딩. Repository 구현이 둘 중 하나를 고른다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds @PleosSource
    abstract fun bindPleosVehicle(impl: PleosVehicleDataSource): VehicleDataSource

    @Binds @DemoSource
    abstract fun bindDemoVehicle(impl: DemoVehicleDataSource): VehicleDataSource

    @Binds @PleosSource
    abstract fun bindPleosNavi(impl: PleosNaviDataSource): NaviDataSource

    @Binds @DemoSource
    abstract fun bindDemoNavi(impl: DemoNaviDataSource): NaviDataSource

    @Binds @PleosSource
    abstract fun bindPleosLocation(impl: PleosLocationDataSource): LocationDataSource

    @Binds @DemoSource
    abstract fun bindDemoLocation(impl: DemoLocationDataSource): LocationDataSource

    @Binds @PleosSource
    abstract fun bindPleosSpeechOutput(impl: PleosSpeechOutputDataSource): SpeechOutputDataSource

    @Binds @PlatformSource
    abstract fun bindPlatformSpeechOutput(impl: AndroidSpeechOutputDataSource): SpeechOutputDataSource

    @Binds @PleosSource
    abstract fun bindPleosSpeechInput(impl: PleosSpeechInputDataSource): SpeechInputDataSource

    @Binds @PlatformSource
    abstract fun bindPlatformSpeechInput(impl: AndroidSpeechInputDataSource): SpeechInputDataSource
}
