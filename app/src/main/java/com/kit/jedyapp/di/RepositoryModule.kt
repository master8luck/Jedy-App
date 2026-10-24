package com.kit.jedyapp.di

import com.kit.jedyapp.data.repository.OmdbMovieRepository
import com.kit.jedyapp.data.repository.RoomFavoritesRepository
import com.kit.jedyapp.domain.repository.FavoritesRepository
import com.kit.jedyapp.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(implementation: RoomFavoritesRepository): FavoritesRepository


    @Binds
    @Singleton
    abstract fun bindMovieRepository(implementation: OmdbMovieRepository): MovieRepository

}
