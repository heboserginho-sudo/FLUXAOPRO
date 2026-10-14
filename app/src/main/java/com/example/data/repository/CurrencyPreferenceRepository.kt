package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrencyPreferenceRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_currency_prefs", Context.MODE_PRIVATE)

    private val _currentCurrency = MutableStateFlow(loadSavedCurrency())
    val currentCurrency: StateFlow<AppCurrency> = _currentCurrency.asStateFlow()

    private fun loadSavedCurrency(): AppCurrency {
        val savedCode = prefs.getString(KEY_CURRENCY_CODE, AppCurrency.AOA.code)
        return AppCurrency.fromCode(savedCode)
    }

    fun setCurrency(currency: AppCurrency) {
        prefs.edit().putString(KEY_CURRENCY_CODE, currency.code).apply()
        _currentCurrency.value = currency
    }

    companion object {
        private const val KEY_CURRENCY_CODE = "selected_currency_code"
    }
}
