package com.kit.jedyapp.data.remote

/** Wire field names match OMDb; converter configuration follows later. */
@Suppress("PropertyName")
data class OmdbSearchResponse(
    val Search: List<OmdbMovieResponse>? = null,
    val totalResults: String? = null,
    val Response: String,
    val Error: String? = null,
)
