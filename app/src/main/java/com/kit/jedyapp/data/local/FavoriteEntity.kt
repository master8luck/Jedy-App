package com.kit.jedyapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kit.jedyapp.domain.model.Movie

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: String,
    val type: String,
    val posterUrl: String?,
    val savedAt: Long = System.currentTimeMillis(),
) {
    fun toMovie() = Movie(id, title, year, type, posterUrl)
}

internal fun Movie.toFavorite() = FavoriteEntity(id, title, year, type, posterUrl)
