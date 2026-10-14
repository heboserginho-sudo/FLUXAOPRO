package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages persistent state for the FluxAO Informative Splash Screen.
 * Ensures the welcome splash is shown ONLY on the first app opening,
 * persists across reboots, sessions and app updates, and supports versioning.
 */
class WelcomeSplashPreferenceRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "fluxao_welcome_splash_prefs"

        /**
         * Versioning for future onboarding/splash iterations.
         * Default: 1.
         */
        const val CURRENT_WELCOME_SPLASH_VERSION = 1

        const val KEY_HAS_SEEN_WELCOME_SPLASH = "hasSeenWelcomeSplash"
        const val KEY_WELCOME_SPLASH_VERSION = "welcomeSplashVersion"
    }

    private val _hasSeenWelcomeSplash = MutableStateFlow(loadHasSeenWelcomeSplash())
    val hasSeenWelcomeSplash: StateFlow<Boolean> = _hasSeenWelcomeSplash.asStateFlow()

    /**
     * Synchronous check on startup. Returns true if user has already completed the splash.
     */
    fun loadHasSeenWelcomeSplash(): Boolean {
        val hasSeen = prefs.getBoolean(KEY_HAS_SEEN_WELCOME_SPLASH, false)
        val savedVersion = prefs.getInt(KEY_WELCOME_SPLASH_VERSION, 0)
        return hasSeen && savedVersion >= CURRENT_WELCOME_SPLASH_VERSION
    }

    /**
     * Marks the welcome splash as completed persistently in SharedPreferences.
     */
    fun markWelcomeSplashCompleted() {
        prefs.edit()
            .putBoolean(KEY_HAS_SEEN_WELCOME_SPLASH, true)
            .putInt(KEY_WELCOME_SPLASH_VERSION, CURRENT_WELCOME_SPLASH_VERSION)
            .apply()
        _hasSeenWelcomeSplash.value = true
    }

    /**
     * Resets the splash state (used for testing or explicit onboarding replay).
     */
    fun resetWelcomeSplash() {
        prefs.edit()
            .remove(KEY_HAS_SEEN_WELCOME_SPLASH)
            .remove(KEY_WELCOME_SPLASH_VERSION)
            .apply()
        _hasSeenWelcomeSplash.value = false
    }
}
