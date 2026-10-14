package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferenceRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    private val _currentThemeMode = MutableStateFlow(loadSavedTheme())
    val currentThemeMode: StateFlow<AppThemeMode> = _currentThemeMode.asStateFlow()

    private fun loadSavedTheme(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name)
        return try {
            AppThemeMode.valueOf(savedName ?: AppThemeMode.LIGHT.name)
        } catch (e: Exception) {
            AppThemeMode.LIGHT
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _currentThemeMode.value = mode
    }

    fun toggleTheme() {
        val nextMode = when (_currentThemeMode.value) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    companion object {
        private const val KEY_THEME_MODE = "selected_theme_mode"
    }
}
