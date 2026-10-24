package com.kit.jedyapp.domain.repository

import com.kit.jedyapp.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Movie>>
    suspend fun addFavorite(movie: Movie)
    suspend fun removeFavorite(id: String)
}
