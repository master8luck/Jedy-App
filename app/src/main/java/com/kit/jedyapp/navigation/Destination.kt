package com.kit.jedyapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Search : NavKey

@Serializable
data object Favorites : NavKey

@Serializable
data class MovieDetails(val imdbId: String) : NavKey
