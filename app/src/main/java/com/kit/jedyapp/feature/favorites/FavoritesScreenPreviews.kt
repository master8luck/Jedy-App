package com.kit.jedyapp.feature.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kit.jedyapp.ui.preview.PreviewMovies
import com.kit.jedyapp.ui.preview.PreviewScreen
import com.kit.jedyapp.ui.preview.ScreenLayoutPreviews

class FavoritesPreviewProvider : PreviewParameterProvider<FavoritesUiState> {
    private val movies = PreviewMovies.movies(12)
    private val cases = listOf(
        "Loading" to FavoritesUiState(),
        "Empty collection" to FavoritesUiState(loading = false),
        "One favorite" to FavoritesUiState(movies.take(1), loading = false),
        "Many favorites · long titles" to FavoritesUiState(movies, loading = false),
        "Update error" to FavoritesUiState(movies.take(1), loading = false, error = true),
        "Removing · action disabled" to FavoritesUiState(movies.take(1), loading = false,
            removing = setOf(movies.first().id)),
    )
    override val values = cases.asSequence().map { it.second }
    override fun getDisplayName(index: Int): String = cases[index].first
}

@Preview(name = "Favorites states", group = "Favorites states", widthDp = 390, heightDp = 700)
@Composable
private fun FavoritesStatesPreview(@PreviewParameter(FavoritesPreviewProvider::class) state: FavoritesUiState) {
    PreviewScreen { FavoritesScreen(state, {}, {}) }
}

@ScreenLayoutPreviews
@Composable
private fun FavoritesLayoutPreview() {
    PreviewScreen { FavoritesScreen(FavoritesUiState(PreviewMovies.movies(12), loading = false), {}, {}) }
}
