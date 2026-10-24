package com.kit.jedyapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kit.jedyapp.R
import com.kit.jedyapp.domain.model.Movie

@Composable
fun MoviePoster(url: String?, modifier: Modifier = Modifier) {
    Surface(modifier.aspectRatio(2f / 3f), shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        AsyncImage(
            model = url, contentDescription = null, contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.poster_placeholder),
            error = painterResource(R.drawable.poster_placeholder),
            fallback = painterResource(R.drawable.poster_placeholder),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            MoviePoster(movie.posterUrl, Modifier.width(76.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium, maxLines = 4,
                    overflow = TextOverflow.Ellipsis)
                Text(listOf(movie.year, movie.type).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing?.invoke()
        }
    }
}
