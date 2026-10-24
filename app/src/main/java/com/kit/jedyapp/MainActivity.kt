package com.kit.jedyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kit.jedyapp.navigation.JedyApp
import com.kit.jedyapp.ui.theme.JedyAppTheme

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JedyAppTheme {
                JedyApp()
            }
        }
    }
}
