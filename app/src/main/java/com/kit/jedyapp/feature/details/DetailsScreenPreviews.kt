package com.kit.jedyapp.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kit.jedyapp.ui.preview.PreviewMovies
import com.kit.jedyapp.ui.preview.PreviewScreen
import com.kit.jedyapp.ui.preview.ScreenLayoutPreviews

class DetailsPreviewProvider : PreviewParameterProvider<DetailsUiState> {
    private val loaded = DetailsUiState(movie = PreviewMovies.movies(1).first(), loading = false)
    private val cases = listOf(
        "Loading" to DetailsUiState(),
        "Load error · retry" to DetailsUiState(loading = false, loadError = true),
        "Movie · not saved" to loaded,
        "Movie · saved" to loaded.copy(isFavorite = true),
        "Saving · action disabled" to loaded.copy(saving = true),
        "Save error" to loaded.copy(saveError = true),
        "Long title · no poster" to loaded.copy(movie = PreviewMovies.longMovie),
    )
    override val values = cases.asSequence().map { it.second }
    override fun getDisplayName(index: Int): String = cases[index].first
}

@Preview(name = "Details states", group = "Details states", widthDp = 390, heightDp = 700)
@Composable
private fun DetailsStatesPreview(@PreviewParameter(DetailsPreviewProvider::class) state: DetailsUiState) {
    PreviewScreen { DetailsScreen(state, {}, {}) }
}

@ScreenLayoutPreviews
@Composable
private fun DetailsLayoutPreview() {
    PreviewScreen { DetailsScreen(DetailsUiState(movie = PreviewMovies.longMovie, loading = false), {}, {}) }
}
