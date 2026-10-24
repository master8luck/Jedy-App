package com.kit.jedyapp.domain.repository

import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.model.SearchPage

interface MovieRepository {
    suspend fun searchMovies(query: String, page: Int = 1): SearchPage
    suspend fun getMovie(id: String): Movie
}
