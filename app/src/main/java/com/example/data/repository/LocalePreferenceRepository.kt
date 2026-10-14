package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.CountryDirectory
import com.example.data.i18n.I18nService
import com.example.data.i18n.TimezoneService
import com.example.data.model.AppCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalePreferenceRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_locale_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(loadInitialLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _currentCountry = MutableStateFlow(loadInitialCountry())
    val currentCountry: StateFlow<String> = _currentCountry.asStateFlow()

    private val _currentTimezone = MutableStateFlow(loadInitialTimezone())
    val currentTimezone: StateFlow<String> = _currentTimezone.asStateFlow()

    private val _secondaryCurrency = MutableStateFlow(loadInitialSecondaryCurrency())
    val secondaryCurrency: StateFlow<AppCurrency> = _secondaryCurrency.asStateFlow()

    init {
        // Synchronize with I18nService
        I18nService.setLanguage(_currentLanguage.value)
    }

    private fun loadInitialLanguage(): AppLanguage {
        val saved = prefs.getString(KEY_LANGUAGE, null)
        return if (saved != null) {
            AppLanguage.fromCode(saved)
        } else {
            // Automatic detection on first launch
            val detected = AppLanguage.detectDeviceLanguage()
            prefs.edit().putString(KEY_LANGUAGE, detected.code).apply()
            detected
        }
    }

    private fun loadInitialCountry(): String {
        return prefs.getString(KEY_COUNTRY, "AO") ?: "AO"
    }

    private fun loadInitialTimezone(): String {
        return prefs.getString(KEY_TIMEZONE, "Africa/Luanda") ?: "Africa/Luanda"
    }

    private fun loadInitialSecondaryCurrency(): AppCurrency {
        val saved = prefs.getString(KEY_SECONDARY_CURRENCY, AppCurrency.USD.code)
        return AppCurrency.fromCode(saved)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
        I18nService.setLanguage(language)
    }

    fun setCountry(countryIsoCode: String) {
        val clean = countryIsoCode.uppercase().trim().take(2)
        prefs.edit().putString(KEY_COUNTRY, clean).apply()
        _currentCountry.value = clean

        // Automatically align timezone if default
        val country = CountryDirectory.getByCode(clean)
        setTimezone(country.defaultTimezone)
    }

    fun setTimezone(timezoneId: String) {
        prefs.edit().putString(KEY_TIMEZONE, timezoneId).apply()
        _currentTimezone.value = timezoneId
    }

    fun setSecondaryCurrency(currency: AppCurrency) {
        prefs.edit().putString(KEY_SECONDARY_CURRENCY, currency.code).apply()
        _secondaryCurrency.value = currency
    }

    companion object {
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_COUNTRY = "selected_country"
        private const val KEY_TIMEZONE = "selected_timezone"
        private const val KEY_SECONDARY_CURRENCY = "selected_secondary_currency"
    }
}
