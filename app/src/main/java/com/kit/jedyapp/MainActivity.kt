package com.kit.jedyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kit.jedyapp.navigation.JedyApp
import com.kit.jedyapp.ui.theme.JedyAppTheme
import com.kit.jedyapp.ui.theme.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mode by themePreferences.mode.collectAsStateWithLifecycle()
            JedyAppTheme(mode) {
                JedyApp(mode, themePreferences::setMode)
            }
        }
    }
}
