package com.kit.jedyapp.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kit.jedyapp.domain.model.Movie
import com.kit.jedyapp.ui.theme.JedyAppTheme

/** Sizes represent screen content after the app's navigation chrome. */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "Short landscape", group = "Layout checks", widthDp = 800, heightDp = 220)
@Preview(name = "Tablet pane", group = "Layout checks", widthDp = 700, heightDp = 600)
@Preview(name = "Large font", group = "Layout checks", widthDp = 390, heightDp = 700, fontScale = 1.5f)
@Preview(name = "Dark", group = "Layout checks", widthDp = 390, heightDp = 700,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ScreenLayoutPreviews

@Composable
internal fun PreviewScreen(content: @Composable () -> Unit) {
    JedyAppTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
    }
}

/** No remote poster URLs: preview rendering must remain deterministic and offline. */
internal object PreviewMovies {
    val longTitle = "The Extraordinary Story of a Very Long Movie Title That Must Remain Readable"
    private val titles = listOf("The Matrix", "The Matrix Reloaded", longTitle, "Up", "Arrival", "The Grand Budapest Hotel")
    fun movies(count: Int): List<Movie> = List(count) { index ->
        Movie("preview-$index", titles[index % titles.size], if (index % 2 == 0) "1999" else "2020–2026",
            if (index % 2 == 0) "movie" else "series", posterUrl = null)
    }
    val longMovie = Movie("preview-long", longTitle, "2020–2026", "series", posterUrl = null)
}

/** Design-time representation only; deliberately never loads a real SDK ad. */
@Composable
internal fun PreviewNativeAd(slot: Int) {
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sponsored · preview slot ${slot + 1}", style = MaterialTheme.typography.labelSmall)
            Text("Native advertisement", style = MaterialTheme.typography.titleMedium)
        }
    }
}
