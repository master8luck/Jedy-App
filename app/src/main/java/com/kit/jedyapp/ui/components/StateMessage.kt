package com.kit.jedyapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.kit.jedyapp.ui.preview.PreviewScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeading(title: String, subtitle: String) {
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StateMessage(
    title: String, message: String, modifier: Modifier = Modifier,
    action: String? = null, onAction: () -> Unit = {},
) {
    Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (action != null) Button(onClick = onAction) { Text(action) }
    }
}

@Preview(name = "Section heading", widthDp = 360, heightDp = 160)
@Composable
private fun SectionHeadingPreview() {
    PreviewScreen { SectionHeading("Discover movies", "Search by title and find your next favorite.") }
}

@Preview(name = "Empty state", widthDp = 360, heightDp = 240)
@Composable
private fun StateMessagePreview() {
    PreviewScreen { StateMessage("No movies found", "Try another movie title.") }
}

@Preview(name = "Error with retry", widthDp = 320, heightDp = 360, fontScale = 1.5f)
@Composable
private fun StateMessageRetryPreview() {
    PreviewScreen {
        StateMessage("Unable to load movies", "Check your connection and try again.", action = "Retry")
    }
}
