package com.kit.jedyapp.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import coil3.compose.AsyncImage
import com.kit.jedyapp.R
import com.kit.jedyapp.domain.model.Movie

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onFavoritesClick: () -> Unit,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberSaveable(state.query.trim(), saver = LazyListState.Saver) {
        LazyListState()
    }
    Column(
        modifier.fillMaxSize().imePadding()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 840.dp).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.search_title), style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onFavoritesClick) { Text(stringResource(R.string.favorites_title)) }
        }
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboard?.hide()
                onSearch()
            }),
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    TextButton(onClick = { onQueryChanged("") }) {
                        Text(stringResource(R.string.clear_search))
                    }
                }
            },
        )
        when (val content = state.content) {
            SearchContent.Idle -> Text(stringResource(R.string.search_hint))
            SearchContent.Loading -> {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(stringResource(R.string.search_loading))
            }
            is SearchContent.Error -> {
                val message = when (content.reason) {
                    SearchError.Network -> R.string.search_network_error
                    SearchError.Service -> R.string.search_service_error
                    SearchError.Unexpected -> R.string.search_unexpected_error
                }
                Text(stringResource(message), color = MaterialTheme.colorScheme.error)
                Button(onClick = onSearch) { Text(stringResource(R.string.retry)) }
            }
            is SearchContent.Results -> {
                if (content.movies.isEmpty()) {
                    Text(stringResource(R.string.search_empty))
                } else {
                    Text(
                        stringResource(R.string.search_result_count, content.movies.size, content.totalResults),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        state = listState,
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(content.movies, key = { it.id }, contentType = { "movie" }) { movie ->
                            MovieRow(movie = movie, onClick = {
                                keyboard?.hide()
                                onMovieClick(movie.id)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieRow(movie: Movie, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                placeholder = painterResource(R.drawable.poster_placeholder),
                error = painterResource(R.drawable.poster_placeholder),
                fallback = painterResource(R.drawable.poster_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(72.dp).aspectRatio(2f / 3f),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    listOf(movie.year, movie.type).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
