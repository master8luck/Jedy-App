package com.kit.jedyapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesDaoTest {
    @Test fun favoritesAreUniqueAndRemovable() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), MovieDatabase::class.java
        ).build()
        try {
            val dao = database.favoritesDao()
            val favorite = FavoriteEntity("tt1", "Movie", "2020", "movie", null)
            dao.insert(favorite)
            dao.insert(favorite)
            assertEquals(1, dao.observeFavorites().first().size)
            dao.delete("tt1")
            assertTrue(dao.observeFavorites().first().isEmpty())
        } finally { database.close() }
    }
}
