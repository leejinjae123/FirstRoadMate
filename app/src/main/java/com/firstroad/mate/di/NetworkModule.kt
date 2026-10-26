package com.firstroad.mate.di

import com.firstroad.mate.BuildConfig
import com.firstroad.mate.data.network.common.ApiResultCallAdapterFactory
import com.firstroad.mate.data.network.place.PlaceService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** 장소 검색 API 전용 클라이언트 (서버가 늘어나면 Qualifier로 구분한다). */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class PlaceApi

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton @PlaceApi
    fun providePlaceOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "KakaoAK ${BuildConfig.PLACE_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    @Provides @Singleton @PlaceApi
    fun providePlaceRetrofit(@PlaceApi client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.PLACE_API_BASE_URL)
        .client(client)
        .addCallAdapterFactory(ApiResultCallAdapterFactory())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun providePlaceService(@PlaceApi retrofit: Retrofit): PlaceService = retrofit.create(PlaceService::class.java)

    private const val TIMEOUT_SECONDS = 10L
}
