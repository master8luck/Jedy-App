package com.kit.jedyapp.feature.search

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.ui.NativeAdSlot

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kit.jedyapp.ui.preview.PreviewMovies
import com.kit.jedyapp.ui.preview.PreviewNativeAd
import com.kit.jedyapp.ui.preview.PreviewScreen
import com.kit.jedyapp.ui.preview.ScreenLayoutPreviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kit.jedyapp.R
import com.kit.jedyapp.sdk.policy.FeedEntry
import com.kit.jedyapp.sdk.policy.buildAdFeed
import com.kit.jedyapp.ui.components.MovieCard
import com.kit.jedyapp.ui.components.SectionHeading
import com.kit.jedyapp.ui.components.StateMessage

@Composable
fun SearchRoute(onMovieClick: (String) -> Unit, adsProvider: AdsProvider) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(state, viewModel::onQueryChanged, viewModel::onSearch, onMovieClick,
        nativeAd = { slot -> NativeAdSlot(adsProvider, "${state.query.trim()}:$slot") },
        onLoadMore = viewModel::loadMore)
}

@OptIn(ExperimentalFoundationApi::class)
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
    val results = state.content as? SearchContent.Results
    val loadMore = rememberUpdatedState(onLoadMore)
    val moviePositions = remember(results?.movies) {
        results?.movies?.mapIndexed { index, movie -> movie.id to index }?.toMap().orEmpty()
    }

    LaunchedEffect(listState, results) {
        if (results == null || results.movies.isEmpty() || results.loadingMore || results.appendError ||
            results.movies.size >= results.totalResults || results.page >= 100
        ) return@LaunchedEffect
        // Count movies, not headers or ads. Start when the last 10% enters the viewport.
        val threshold = results.movies.size - (results.movies.size + 9) / 10
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.any { item ->
                val position = moviePositions[item.key]
                position != null && position >= threshold &&
                        item.offset < listState.layoutInfo.viewportEndOffset && item.offset + item.size > 0
            }
        }.distinctUntilChanged().collect { nearEnd ->
            if (nearEnd) loadMore.value()
        }
    }

    Box(modifier
        .fillMaxSize()
        .imePadding(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .clipToBounds()
                .testTag("search-list"),
            state = listState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = INTRODUCTION_KEY, contentType = "heading") {
                SectionHeading(
                    stringResource(R.string.discover_title),
                    stringResource(R.string.discover_subtitle)
                )
            }

            stickyHeader(key = SEARCH_INPUT_KEY) {
                // An opaque pinned header keeps scrolled native views behind the search field.
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .zIndex(1f),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchInput(
                        query = state.query,
                        onQueryChanged = onQueryChanged,
                        onSearch = { keyboard?.hide(); onSearch() },
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            when (val content = state.content) {
                SearchContent.Idle -> item(key = IDLE_KEY) {
                    StateMessage(
                        stringResource(R.string.search_idle_title),
                        stringResource(R.string.search_hint),
                        Modifier.fillMaxWidth()
                    )
                }

                SearchContent.Loading -> item(key = LOADING_KEY) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text(
                            stringResource(R.string.search_loading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is SearchContent.Error -> item(key = ERROR_KEY) {
                    val message = when (content.reason) {
                        SearchError.Network -> R.string.search_network_error
                        SearchError.Service -> R.string.search_service_error
                        SearchError.Unexpected -> R.string.search_unexpected_error
                    }
                    StateMessage(
                        stringResource(R.string.search_error_title), stringResource(message),
                        Modifier.fillMaxWidth(), stringResource(R.string.retry), onSearch
                    )
                }

                is SearchContent.Results -> {
                    if (content.movies.isEmpty()) {
                        item(key = EMPTY_KEY) {
                            StateMessage(
                                stringResource(R.string.search_empty),
                                stringResource(R.string.search_empty_hint),
                                Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        item(key = RESULT_COUNT_KEY) {
                            Text(
                                stringResource(
                                    R.string.search_result_count,
                                    content.movies.size,
                                    content.totalResults
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(
                            buildAdFeed(content.movies),
                            key = { entry ->
                                when (entry) {
                                    is FeedEntry.Content -> entry.value.id
                                    is FeedEntry.Native -> "$NATIVE_KEY_PREFIX${entry.slot}"
                                }
                            },
                            contentType = { if (it is FeedEntry.Native) "native" else "movie" },
                        ) { entry ->
                            when (entry) {
                                is FeedEntry.Content -> MovieCard(
                                    entry.value,
                                    onClick = { keyboard?.hide(); onMovieClick(entry.value.id) })

                                is FeedEntry.Native -> nativeAd(entry.slot)
                            }
                        }
                        if (content.movies.size < content.totalResults && content.page < 100) {
                            item(key = LOAD_MORE_KEY, contentType = "footer") {
                                OutlinedButton(
                                    onClick = onLoadMore, enabled = !content.loadingMore,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        stringResource(
                                            when {
                                                content.loadingMore -> R.string.search_loading
                                                content.appendError -> R.string.load_more_retry
                                                else -> R.string.load_more
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query, onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .testTag("search-input"),
        label = { Text(stringResource(R.string.search_label)) }, singleLine = true,
        shape = MaterialTheme.shapes.large,
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChanged("") }) {
                Icon(painterResource(R.drawable.ic_close), stringResource(R.string.clear_search))
            }
        },
    )
}

private const val INTRODUCTION_KEY = "introduction"
private const val SEARCH_INPUT_KEY = "search-input"
private const val IDLE_KEY = "idle"
private const val LOADING_KEY = "loading"
private const val ERROR_KEY = "error"
private const val EMPTY_KEY = "empty"
private const val RESULT_COUNT_KEY = "result-count"
private const val LOAD_MORE_KEY = "load-more"
private const val NATIVE_KEY_PREFIX = "native:"

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
        "Loading next page" to SearchUiState(
            "Matrix",
            SearchContent.Results(PreviewMovies.movies(3), 30, loadingMore = true)
        ),
        "Next page failed" to SearchUiState(
            "Matrix",
            SearchContent.Results(PreviewMovies.movies(3), 30, appendError = true)
        ),
        "Long title · no poster" to SearchUiState(
            "long",
            SearchContent.Results(listOf(PreviewMovies.longMovie), 1)
        ),
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

@Preview(
    name = "3 movies and both native slots",
    group = "Search ads",
    widthDp = 390,
    heightDp = 1120
)
@Composable
private fun SearchAdPlacementPreview() {
    PreviewScreen {
        SearchScreen(
            SearchUiState("Matrix", SearchContent.Results(PreviewMovies.movies(3), 3)),
            {}, {}, {}, nativeAd = { PreviewNativeAd(it) },
        )
    }
}
