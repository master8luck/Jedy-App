package com.kit.jedyapp.di

import android.content.Context
import androidx.room.Room
import com.kit.jedyapp.data.local.FavoritesDao
import com.kit.jedyapp.data.local.MovieDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MovieDatabase =
        Room.databaseBuilder(context, MovieDatabase::class.java, "movies.db").build()

    @Provides
    fun provideFavoritesDao(database: MovieDatabase): FavoritesDao = database.favoritesDao()
}
