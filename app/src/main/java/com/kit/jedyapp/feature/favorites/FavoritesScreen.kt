package com.kit.jedyapp.feature.favorites

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
        item(key = "heading") {
            SectionHeading(stringResource(R.string.favorites_title), stringResource(R.string.favorites_subtitle))
        }
        if (state.error) item(key = "error") {
            Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
        }
        when {
            state.loading -> item(key = "loading") { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.movies.isEmpty() -> item(key = "empty") {
                StateMessage(stringResource(R.string.favorites_empty_title),
                    stringResource(R.string.favorites_empty_hint), modifier = Modifier.fillMaxWidth())
            }
            else -> items(state.movies, key = { it.id }, contentType = { "movie" }) { movie ->
                MovieCard(movie, onClick = { onMovieClick(movie.id) }) {
                    IconButton(onClick = { onRemove(movie.id) }, enabled = movie.id !in state.removing) {
                        Icon(painterResource(R.drawable.ic_favorite),
                            contentDescription = stringResource(R.string.remove_favorite_title, movie.title),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
