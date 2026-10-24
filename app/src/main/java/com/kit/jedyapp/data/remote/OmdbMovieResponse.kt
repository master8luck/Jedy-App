package com.kit.jedyapp.data.remote

@Suppress("PropertyName")
data class OmdbMovieResponse(
    val imdbID: String? = null,
    val Title: String? = null,
    val Year: String? = null,
    val Type: String? = null,
    val Poster: String? = null,
    val Response: String? = null,
    val Error: String? = null,
)
