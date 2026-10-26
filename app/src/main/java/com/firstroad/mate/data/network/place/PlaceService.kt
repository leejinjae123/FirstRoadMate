package com.firstroad.mate.data.network.place

import com.firstroad.mate.data.network.common.ApiResult
import com.firstroad.mate.data.network.place.dto.PlaceSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** 장소 검색 API (카카오 로컬). 인증 헤더는 OkHttp 인터셉터가 붙인다. */
interface PlaceService {

    /** 카테고리 검색 (PK6 주차장, OL7 주유소·충전소). */
    @GET("v2/local/search/category.json")
    suspend fun searchByCategory(
        @Query("category_group_code") categoryCode: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radiusMeters: Int,
        @Query("sort") sort: String = "distance",
    ): ApiResult<PlaceSearchResponseDto>

    /** 키워드 검색 (휴게소, 정비소, 전기차 충전소처럼 카테고리가 없는 장소). */
    @GET("v2/local/search/keyword.json")
    suspend fun searchByKeyword(
        @Query("query") keyword: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radiusMeters: Int,
        @Query("sort") sort: String = "distance",
    ): ApiResult<PlaceSearchResponseDto>
}
