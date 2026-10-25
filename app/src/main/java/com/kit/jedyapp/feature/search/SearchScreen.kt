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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kit.jedyapp.R
import com.kit.jedyapp.sdk.policy.FeedEntry
import com.kit.jedyapp.sdk.policy.buildAdFeed
import com.kit.jedyapp.ui.components.MovieCard
import com.kit.jedyapp.ui.components.SectionHeading
import com.kit.jedyapp.ui.components.StateMessage

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    nativeAd: @Composable (Int) -> Unit = {},
    onLoadMore: () -> Unit = {},
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberSaveable(state.query.trim(), saver = LazyListState.Saver) { LazyListState() }
    Column(
        modifier.fillMaxSize().imePadding().wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 840.dp).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeading(stringResource(R.string.discover_title), stringResource(R.string.discover_subtitle))
        OutlinedTextField(
            value = state.query, onValueChange = onQueryChanged, modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search_label)) }, singleLine = true,
            shape = MaterialTheme.shapes.large,
            leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide(); onSearch() }),
            trailingIcon = {
                if (state.query.isNotEmpty()) IconButton(onClick = { onQueryChanged("") }) {
                    Icon(painterResource(R.drawable.ic_close), stringResource(R.string.clear_search))
                }
            },
        )
        when (val content = state.content) {
            SearchContent.Idle -> StateMessage(
                stringResource(R.string.search_idle_title), stringResource(R.string.search_hint),
                Modifier.weight(1f).fillMaxWidth(),
            )
            SearchContent.Loading -> {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(stringResource(R.string.search_loading), style = MaterialTheme.typography.bodyMedium)
            }
            is SearchContent.Error -> {
                val message = when (content.reason) {
                    SearchError.Network -> R.string.search_network_error
                    SearchError.Service -> R.string.search_service_error
                    SearchError.Unexpected -> R.string.search_unexpected_error
                }
                StateMessage(stringResource(R.string.search_error_title), stringResource(message),
                    Modifier.weight(1f).fillMaxWidth(), stringResource(R.string.retry), onSearch)
            }
            is SearchContent.Results -> {
                if (content.movies.isEmpty()) {
                    StateMessage(stringResource(R.string.search_empty), stringResource(R.string.search_empty_hint),
                        Modifier.weight(1f).fillMaxWidth())
                } else {
                    Text(stringResource(R.string.search_result_count, content.movies.size, content.totalResults),
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds(), state = listState,
                        contentPadding = PaddingValues(bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            buildAdFeed(content.movies),
                            key = { entry -> when (entry) {
                                is FeedEntry.Content -> entry.value.id
                                is FeedEntry.Native -> "native:${entry.slot}"
                            } },
                            contentType = { if (it is FeedEntry.Native) "native" else "movie" },
                        ) { entry ->
                            when (entry) {
                                is FeedEntry.Content -> MovieCard(entry.value,
                                    onClick = { keyboard?.hide(); onMovieClick(entry.value.id) })
                                is FeedEntry.Native -> nativeAd(entry.slot)
                            }
                        }
                        if (content.movies.size < content.totalResults && content.page < 100) {
                            item(key = "load-more", contentType = "footer") {
                                OutlinedButton(
                                    onClick = onLoadMore, enabled = !content.loadingMore,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(when {
                                        content.loadingMore -> R.string.search_loading
                                        content.appendError -> R.string.load_more_retry
                                        else -> R.string.load_more
                                    }))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
