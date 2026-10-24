package com.kit.jedyapp.sdk.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kit.jedyapp.sdk.R
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.api.NativeAdHandle

@Composable
fun NativeAdSlot(provider: AdsProvider, slotKey: String, modifier: Modifier = Modifier) {
    var handle by remember(provider, slotKey) { mutableStateOf<NativeAdHandle?>(null) }
    var loading by remember(provider, slotKey) { mutableStateOf(true) }
    DisposableEffect(provider, slotKey) {
        var disposed = false
        provider.loadNative { loaded ->
            if (disposed) loaded?.close() else {
                handle?.close()
                handle = loaded
                loading = false
            }
        }
        onDispose { disposed = true; handle?.close(); handle = null }
    }
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()
    handle?.let { ad ->
        // Recreate native asset views when appearance changes; keep ownership of the same ad.
        key(ad, textColor, surfaceColor) {
            Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                AndroidView(
                    factory = { ad.createView(it, textColor, surfaceColor) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } ?: if (loading) {
        Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.ad_loading), style = MaterialTheme.typography.labelSmall)
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    } else Unit
}
