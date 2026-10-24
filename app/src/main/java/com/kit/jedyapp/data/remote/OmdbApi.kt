package com.kit.jedyapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** API key is attached by the HTTP client, not passed through the domain layer. */
interface OmdbApi {
    @GET(".")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("page") page: Int = 1,
    ): OmdbSearchResponse

    @GET(".")
    suspend fun getMovie(@Query("i") imdbId: String): OmdbMovieResponse
}
