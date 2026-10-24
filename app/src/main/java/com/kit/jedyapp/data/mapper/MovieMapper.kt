package com.kit.jedyapp.data.mapper

import com.kit.jedyapp.data.remote.OmdbMovieResponse
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.model.MovieDataException

internal fun OmdbMovieResponse.toMovie(): Movie {
    val id = imdbId?.takeIf { it.isNotBlank() }
        ?: throw MovieDataException("Movie ID is missing.")
    val movieTitle = title?.takeIf { it.isNotBlank() }
        ?: throw MovieDataException("Movie title is missing.")
    return Movie(
        id = id,
        title = movieTitle,
        year = year.orEmpty(),
        type = type.orEmpty(),
        posterUrl = poster?.takeUnless { it.isBlank() || it == "N/A" },
    )
}
