package com.kit.jedyapp.data.remote

/**
 * Endpoint declarations only. HTTP client and JSON conversion follow later.
 * Base URL: https://www.omdbapi.com/
 * The eventual client supplies the API key.
 */
interface OmdbApi {
    /** GET /?s={query}&page={page}&apikey={key} */
    suspend fun searchMovies(query: String, page: Int = 1): OmdbSearchResponse

    /** GET /?i={imdbId}&apikey={key} */
    suspend fun getMovie(imdbId: String): OmdbMovieResponse
}
