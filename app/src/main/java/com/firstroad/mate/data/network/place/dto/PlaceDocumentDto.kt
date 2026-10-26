package com.firstroad.mate.data.network.place.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 장소 검색 API(카카오 로컬)의 장소 한 건. x = 경도, y = 위도 (문자열). */
@Serializable
data class PlaceDocumentDto(
    @SerialName("id") val id: String,
    @SerialName("place_name") val placeName: String,
    @SerialName("address_name") val addressName: String = "",
    @SerialName("road_address_name") val roadAddressName: String = "",
    @SerialName("x") val longitude: String,
    @SerialName("y") val latitude: String,
    @SerialName("distance") val distance: String = "",
)
