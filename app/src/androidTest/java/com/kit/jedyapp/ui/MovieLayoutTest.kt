package com.kit.jedyapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import com.kit.jedyapp.feature.search.SearchScreen
import com.kit.jedyapp.feature.search.SearchUiState
import com.kit.jedyapp.feature.search.SearchContent
import com.kit.jedyapp.feature.search.SearchError
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.feature.details.DetailsScreen
import com.kit.jedyapp.feature.details.DetailsUiState
import com.kit.jedyapp.feature.favorites.FavoritesScreen
import com.kit.jedyapp.feature.favorites.FavoritesUiState
import com.kit.jedyapp.ui.theme.JedyAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MovieLayoutTest {
    @get:Rule val compose = createComposeRule()
    private val longTitle = "The Extraordinary Story of a Very Long Movie Title That Must Remain Readable"
    private val movie = Movie("tt-test", longTitle, "1999–2026", "series", null)

    @Test fun detailsWithLargeTextRemainsScrollableAndActionable() {
        var saved = false
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                JedyAppTheme {
                    Box(Modifier.width(320.dp).height(480.dp)) {
                        DetailsScreen(DetailsUiState(movie = movie, loading = false), {}, { saved = true })
                    }
                }
            }
        }
        compose.onNodeWithText(longTitle).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Save to favorites").performScrollTo().performClick()
        compose.runOnIdle { assertTrue(saved) }
    }

    @Test fun favoriteCardSupportsLongTitleAndSeparateRemoveAction() {
        var removed = false
        compose.setContent {
            JedyAppTheme {
                Box(Modifier.width(320.dp).height(600.dp)) {
                    FavoritesScreen(FavoritesUiState(listOf(movie), loading = false), {}, { removed = true })
                }
            }
        }
        compose.onNodeWithText(longTitle).assertIsDisplayed()
        compose.onNodeWithContentDescription("Remove $longTitle from favorites").performClick()
        compose.runOnIdle { assertTrue(removed) }
    }

    @Test fun searchResultsRemainReachableInShortWindowWithLargeText() {
        renderSearch(searchResults(12), fontScale = 1.5f)
        compose.onNodeWithTag("search-list").performScrollToNode(hasText("Movie 9"))
        compose.onNodeWithText("Movie 9").assertIsDisplayed()
        compose.onNodeWithTag("search-input").assertIsDisplayed()
    }

    @Test fun searchErrorRemainsScrollableToRetryInShortWindow() {
        var retried = false
        renderSearch(
            SearchUiState("Matrix", SearchContent.Error(SearchError.Network)),
            fontScale = 1.5f,
            onSearch = { retried = true },
        )
        compose.onNodeWithText("Retry").performScrollTo().performClick()
        compose.onNodeWithTag("search-input").assertIsDisplayed()
        compose.runOnIdle { assertTrue(retried) }
    }

    @Test fun nativeSlotsAppearOnlyWhenAtLeastThreeMoviesExist() {
        val state = mutableStateOf(searchResults(2))
        compose.setContent {
            JedyAppTheme {
                Box(Modifier.fillMaxWidth().height(220.dp)) {
                    SearchScreen(state.value, {}, {}, {},
                        nativeAd = { Text("Native test slot $it") })
                }
            }
        }
        compose.onAllNodesWithText("Native test slot", substring = true).assertCountEquals(0)
        compose.runOnIdle { state.value = searchResults(3) }
        compose.onNodeWithTag("search-list").performScrollToNode(hasText("Native test slot 1"))
        compose.onNodeWithText("Native test slot 1").assertIsDisplayed()
        compose.onNodeWithTag("search-input").assertIsDisplayed()
    }

    @Test fun nearEndLoadsOnceAndAppendFailureRequiresManualRetry() {
        val initial = searchResults(20)
        val results = (initial.content as SearchContent.Results).copy(totalResults = 40)
        val state = mutableStateOf(initial.copy(content = results))
        var requests = 0
        compose.setContent {
            JedyAppTheme {
                Box(Modifier.fillMaxWidth().height(400.dp)) {
                    SearchScreen(state.value, {}, {}, {}, onLoadMore = {
                        requests++
                        state.value = state.value.copy(content = results.copy(loadingMore = true))
                    })
                }
            }
        }
        compose.onNodeWithTag("search-list").performScrollToNode(hasText("Movie 10"))
        compose.runOnIdle { org.junit.Assert.assertEquals(0, requests) }
        compose.onNodeWithTag("search-list").performScrollToNode(hasText("Movie 18"))
        compose.waitUntil { requests == 1 }
        compose.runOnIdle { state.value = initial.copy(content = results.copy(appendError = true)) }
        compose.onNodeWithTag("search-list").performScrollToNode(hasText("Movie 19"))
        compose.runOnIdle { org.junit.Assert.assertEquals(1, requests) }
        compose.onNodeWithTag("search-list").performTouchInput { swipeUp() }
        compose.onNodeWithText("Retry loading more").assertIsDisplayed().performClick()
        compose.runOnIdle { org.junit.Assert.assertEquals(2, requests) }
    }

    private fun searchResults(count: Int) = SearchUiState(
        "Matrix", SearchContent.Results(
            List(count) { Movie("test-$it", "Movie $it", "2020", "movie", null) }, count
        )
    )

    private fun renderSearch(state: SearchUiState, fontScale: Float, onSearch: () -> Unit = {}) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                JedyAppTheme {
                    Box(Modifier.fillMaxWidth().height(220.dp)) {
                        SearchScreen(state, {}, onSearch, {},
                            nativeAd = { Text("Native test slot $it") })
                    }
                }
            }
        }
    }
}
