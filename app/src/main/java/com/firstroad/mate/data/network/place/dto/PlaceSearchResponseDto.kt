package com.firstroad.mate.data.network.place.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceSearchResponseDto(
    @SerialName("documents") val documents: List<PlaceDocumentDto> = emptyList(),
)
