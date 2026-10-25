package com.kit.jedyapp.feature.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kit.jedyapp.ui.preview.PreviewMovies
import com.kit.jedyapp.ui.preview.PreviewScreen
import com.kit.jedyapp.ui.preview.ScreenLayoutPreviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kit.jedyapp.R
import com.kit.jedyapp.ui.components.MovieCard
import com.kit.jedyapp.ui.components.SectionHeading
import com.kit.jedyapp.ui.components.StateMessage

@Composable
fun FavoritesRoute(onMovieClick: (String) -> Unit) {
    val viewModel: FavoritesViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FavoritesScreen(state, onMovieClick, viewModel::remove)
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onMovieClick: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = HEADING_KEY) {
            SectionHeading(
                stringResource(R.string.favorites_title),
                stringResource(R.string.favorites_subtitle)
            )
        }
        if (state.error) item(key = ERROR_KEY) {
            Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
        }
        when {
            state.loading -> item(key = LOADING_KEY) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.movies.isEmpty() -> item(key = EMPTY_KEY) {
                StateMessage(
                    stringResource(R.string.favorites_empty_title),
                    stringResource(R.string.favorites_empty_hint),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> items(state.movies, key = { it.id }, contentType = { "movie" }) { movie ->
                MovieCard(movie, onClick = { onMovieClick(movie.id) }) {
                    IconButton(
                        onClick = { onRemove(movie.id) },
                        enabled = movie.id !in state.removing
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_favorite),
                            contentDescription = stringResource(
                                R.string.remove_favorite_title,
                                movie.title
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private const val HEADING_KEY = "heading"
private const val ERROR_KEY = "error"
private const val LOADING_KEY = "loading"
private const val EMPTY_KEY = "empty"

class FavoritesPreviewProvider : PreviewParameterProvider<FavoritesUiState> {
    private val movies = PreviewMovies.movies(12)
    private val cases = listOf(
        "Loading" to FavoritesUiState(),
        "Empty collection" to FavoritesUiState(loading = false),
        "One favorite" to FavoritesUiState(movies.take(1), loading = false),
        "Many favorites · long titles" to FavoritesUiState(movies, loading = false),
        "Update error" to FavoritesUiState(movies.take(1), loading = false, error = true),
        "Removing · action disabled" to FavoritesUiState(
            movies.take(1), loading = false,
            removing = setOf(movies.first().id)
        ),
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
    PreviewScreen {
        FavoritesScreen(
            FavoritesUiState(PreviewMovies.movies(12), loading = false),
            {},
            {})
    }
}
