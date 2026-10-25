package com.kit.jedyapp.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kit.jedyapp.ui.preview.PreviewMovies
import com.kit.jedyapp.ui.preview.PreviewNativeAd
import com.kit.jedyapp.ui.preview.PreviewScreen
import com.kit.jedyapp.ui.preview.ScreenLayoutPreviews

class SearchPreviewProvider : PreviewParameterProvider<SearchUiState> {
    private val cases = listOf(
        "Not searched yet" to SearchUiState(),
        "Searching" to SearchUiState("Matrix", SearchContent.Loading),
        "No matches" to SearchUiState("unknown", SearchContent.Results(emptyList(), 0)),
        "Network error" to SearchUiState("Matrix", SearchContent.Error(SearchError.Network)),
        "Service error" to SearchUiState("Matrix", SearchContent.Error(SearchError.Service)),
        "Unexpected error" to SearchUiState("Matrix", SearchContent.Error(SearchError.Unexpected)),
        "1 movie · no ads" to results(1),
        "2 movies · no ads" to results(2),
        "3 movies · 2 ad slots" to results(3),
        "Many movies · 2 ad slots" to results(12),
        "Loading next page" to SearchUiState("Matrix",
            SearchContent.Results(PreviewMovies.movies(3), 30, loadingMore = true)),
        "Next page failed" to SearchUiState("Matrix",
            SearchContent.Results(PreviewMovies.movies(3), 30, appendError = true)),
        "Long title · no poster" to SearchUiState("long",
            SearchContent.Results(listOf(PreviewMovies.longMovie), 1)),
    )
    override val values = cases.asSequence().map { it.second }
    override fun getDisplayName(index: Int): String = cases[index].first

    private fun results(count: Int) =
        SearchUiState("Matrix", SearchContent.Results(PreviewMovies.movies(count), count))
}

@Preview(name = "Search states", group = "Search states", widthDp = 390, heightDp = 700)
@Composable
private fun SearchStatesPreview(@PreviewParameter(SearchPreviewProvider::class) state: SearchUiState) {
    PreviewScreen {
        SearchScreen(state, {}, {}, {}, nativeAd = { PreviewNativeAd(it) })
    }
}

@ScreenLayoutPreviews
@Composable
private fun SearchLayoutPreview() {
    PreviewScreen {
        SearchScreen(
            SearchUiState("Matrix", SearchContent.Results(PreviewMovies.movies(12), 30)),
            {}, {}, {}, nativeAd = { PreviewNativeAd(it) },
        )
    }
}

@Preview(name = "3 movies and both native slots", group = "Search ads", widthDp = 390, heightDp = 1120)
@Composable
private fun SearchAdPlacementPreview() {
    PreviewScreen {
        SearchScreen(
            SearchUiState("Matrix", SearchContent.Results(PreviewMovies.movies(3), 3)),
            {}, {}, {}, nativeAd = { PreviewNativeAd(it) },
        )
    }
}
