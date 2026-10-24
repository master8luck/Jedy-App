package com.kit.jedyapp.data.repository

import com.kit.jedyapp.data.mapper.toMovie
import com.kit.jedyapp.data.remote.OmdbApi
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.model.MovieDataException
import com.kit.jedyapp.domain.model.SearchPage
import com.kit.jedyapp.domain.repository.MovieRepository
import javax.inject.Inject

class OmdbMovieRepository @Inject constructor(
    private val api: OmdbApi,
) : MovieRepository {

    override suspend fun searchMovies(query: String, page: Int): SearchPage {
        require(query.isNotBlank()) { "Search query must not be blank." }
        require(page in 1..100) { "OMDb page must be between 1 and 100." }
        val result = api.searchMovies(query.trim(), page)
        if (result.response == "False" && result.error == "Movie not found!") {
            return SearchPage(emptyList(), 0)
        }
        if (result.response != "True") {
            throw MovieDataException(result.error ?: "Movie search failed.")
        }

        val movies = result.movies ?: throw MovieDataException("Search results are missing.")
        val total = result.totalResults?.toIntOrNull()?.takeIf { it >= movies.size } ?: throw MovieDataException("Search result count is invalid.")
        return SearchPage(movies.map { it.toMovie() }, total)
    }

    override suspend fun getMovie(id: String): Movie {
        require(id.isNotBlank()) { "Movie ID must not be blank." }
        val result = api.getMovie(id)
        if (result.response != "True") {
            throw MovieDataException(result.error ?: "Movie details failed.")
        }
        return result.toMovie()
    }

}
