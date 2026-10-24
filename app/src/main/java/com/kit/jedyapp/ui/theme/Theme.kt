package com.kit.jedyapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val light = lightColorScheme(
    primary = Color(0xFF006B5E), onPrimary = Color.White,
    primaryContainer = Color(0xFFB0F1DF), onPrimaryContainer = Color(0xFF00201B),
    secondaryContainer = Color(0xFFD0E8DE), onSecondaryContainer = Color(0xFF102E25),
    surfaceContainer = Color(0xFFEAEFE9), secondary = Color(0xFF52655E), background = Color(0xFFF8FAF6), surface = Color(0xFFF8FAF6),
    surfaceContainerLow = Color(0xFFF0F4EE), surfaceContainerHigh = Color(0xFFE4EAE3),
)
private val dark = darkColorScheme(
    primary = Color(0xFF79D6BF), onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005045), onPrimaryContainer = Color(0xFFB0F1DF),
    secondaryContainer = Color(0xFF304B40), onSecondaryContainer = Color(0xFFD0E8DE),
    surfaceContainer = Color(0xFF202822), secondary = Color(0xFFB5CCC1), background = Color(0xFF101512), surface = Color(0xFF101512),
    surfaceContainerLow = Color(0xFF191F1B), surfaceContainerHigh = Color(0xFF28302A),
)

@Composable
fun JedyAppTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val darkTheme = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(colorScheme = if (darkTheme) dark else light, typography = Typography, content = content)
}
