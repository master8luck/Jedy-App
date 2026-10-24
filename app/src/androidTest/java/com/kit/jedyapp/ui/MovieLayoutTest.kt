package com.kit.jedyapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.CompositionLocalProvider
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
}
