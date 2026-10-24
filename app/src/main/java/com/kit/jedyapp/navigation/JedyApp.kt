package com.kit.jedyapp.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kit.jedyapp.feature.details.DetailsScreen
import com.kit.jedyapp.feature.favorites.FavoritesScreen
import com.kit.jedyapp.feature.search.SearchScreen

@Composable
fun JedyApp() {
    // Serializable keys keep the back stack across recreation and process restoration.
    val backStack = rememberNavBackStack(Search)
    val goBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            backStack = backStack,
            onBack = goBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<Search> {
                    SearchScreen(
                        onFavoritesClick = { backStack.add(Favorites) },
                        onMovieClick = { id -> backStack.add(MovieDetails(id)) },
                    )
                }
                entry<Favorites> {
                    FavoritesScreen(
                        onBackClick = goBack,
                        onMovieClick = { id -> backStack.add(MovieDetails(id)) },
                    )
                }
                entry<MovieDetails> { destination ->
                    DetailsScreen(
                        imdbId = destination.imdbId,
                        onBackClick = goBack,
                    )
                }
            },
        )
    }
}
