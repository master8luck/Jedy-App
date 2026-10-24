package com.kit.jedyapp.ui.theme

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { System, Light, Dark }

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    private val mutableMode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == preferences.getString("theme", null) } ?: ThemeMode.System
    )
    val mode = mutableMode.asStateFlow()
    fun setMode(mode: ThemeMode) {
        preferences.edit().putString("theme", mode.name).apply()
        mutableMode.value = mode
    }
}
