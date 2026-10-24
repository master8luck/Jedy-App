package com.kit.jedyapp.data.repository

import com.kit.jedyapp.data.local.FavoritesDao
import com.kit.jedyapp.data.local.toFavorite
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository @Inject constructor(
    private val dao: FavoritesDao,
) : FavoritesRepository {
    override fun observeFavorites() = dao.observeFavorites().map { rows -> rows.map { it.toMovie() } }
    override suspend fun addFavorite(movie: Movie) = dao.insert(movie.toFavorite())
    override suspend fun removeFavorite(id: String) = dao.delete(id)
}
