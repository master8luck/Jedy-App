package com.kit.jedyapp.feature.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kit.jedyapp.ui.preview.PreviewMovies
import com.kit.jedyapp.ui.preview.PreviewScreen
import com.kit.jedyapp.ui.preview.ScreenLayoutPreviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kit.jedyapp.R
import com.kit.jedyapp.ui.components.MoviePoster
import com.kit.jedyapp.ui.components.StateMessage

@Composable
fun DetailsRoute(imdbId: String) {
    val viewModel: DetailsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(imdbId) { viewModel.load(imdbId) }
    DetailsScreen(
        state,
        onRetry = { viewModel.load(imdbId) },
        onFavorite = viewModel::toggleFavorite
    )
}

@Composable
fun DetailsScreen(
    state: DetailsUiState,
    onRetry: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        state.loadError -> StateMessage(
            title = stringResource(R.string.details_error),
            message = stringResource(R.string.details_error_hint),
            action = stringResource(R.string.retry),
            onAction = onRetry,
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        )

        else -> state.movie?.let { movie ->
            BoxWithConstraints(modifier.fillMaxSize()) {
                val wide = maxWidth >= 480.dp
                val text: @Composable ColumnScope.() -> Unit = {
                    Text(
                        stringResource(R.string.movie_details_overline),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(movie.title, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        listOf(movie.year, movie.type).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(
                        stringResource(R.string.favorite_explanation),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = onFavorite,
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(if (state.isFavorite) R.string.remove_favorite else R.string.add_favorite))
                    }
                    if (state.saveError) Text(
                        stringResource(R.string.save_error),
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        movie.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (wide) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        MoviePoster(movie.posterUrl, Modifier.weight(0.42f))
                        Column(
                            Modifier.weight(0.58f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            content = text
                        )
                    }
                } else {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        MoviePoster(
                            movie.posterUrl, Modifier
                                .widthIn(max = 240.dp)
                                .fillMaxWidth()
                        )
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            content = text
                        )
                    }
                }

            }
        }
    }
}

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
    PreviewScreen {
        DetailsScreen(
            DetailsUiState(movie = PreviewMovies.longMovie, loading = false),
            {},
            {})
    }
}
