package com.kit.jedyapp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbSearchResponse(
    @SerialName("Search") val movies: List<OmdbMovieResponse>? = null,
    val totalResults: String? = null,
    @SerialName("Response") val response: String,
    @SerialName("Error") val error: String? = null,
)
