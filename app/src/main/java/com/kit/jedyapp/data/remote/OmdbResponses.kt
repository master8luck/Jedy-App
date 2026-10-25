package com.kit.jedyapp.data.remote

import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.model.MovieDataException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbMovieResponse(
    @SerialName("imdbID") val imdbId: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("Year") val year: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("Poster") val poster: String? = null,
    @SerialName("Response") val response: String? = null,
    @SerialName("Error") val error: String? = null,
)

@Serializable
data class OmdbSearchResponse(
    @SerialName("Search") val movies: List<OmdbMovieResponse>? = null,
    val totalResults: String? = null,
    @SerialName("Response") val response: String,
    @SerialName("Error") val error: String? = null,
)

internal fun OmdbMovieResponse.toMovie(): Movie {
    val id = imdbId?.takeIf { it.isNotBlank() } ?: throw MovieDataException("Movie ID is missing.")
    val movieTitle = title?.takeIf { it.isNotBlank() } ?: throw MovieDataException("Movie title is missing.")

    return Movie(
        id = id,
        title = movieTitle,
        year = year.orEmpty(),
        type = type.orEmpty(),
        posterUrl = poster?.takeUnless { it.isBlank() || it == "N/A" },
    )
}
