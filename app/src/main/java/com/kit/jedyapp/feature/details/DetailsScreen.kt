package com.kit.jedyapp.feature.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kit.jedyapp.R

@Composable
fun DetailsScreen(
    imdbId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.details_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.details_placeholder, imdbId))
        TextButton(onClick = onBackClick) { Text(stringResource(R.string.back)) }
    }
}
