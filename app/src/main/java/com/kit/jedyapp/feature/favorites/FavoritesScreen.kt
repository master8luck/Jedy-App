package com.kit.jedyapp.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kit.jedyapp.R

@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.favorites_placeholder))
        // Temporary control to exercise the ID argument before movie data is implemented.
        Button(onClick = { onMovieClick("tt0133093") }) {
            Text(stringResource(R.string.open_sample_movie))
        }
        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.back))
        }
    }
}
