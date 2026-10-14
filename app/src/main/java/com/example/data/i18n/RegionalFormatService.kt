package com.example.data.i18n

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object RegionalFormatService {

    fun getLocaleForLanguageAndCountry(language: AppLanguage, countryCode: String): Locale {
        val cleanCountry = countryCode.uppercase().trim().take(2)
        return when (language) {
            AppLanguage.PORTUGUESE -> {
                when (cleanCountry) {
                    "BR" -> Locale("pt", "BR")
                    "PT" -> Locale("pt", "PT")
                    "MZ" -> Locale("pt", "MZ")
                    "CV" -> Locale("pt", "CV")
                    else -> Locale("pt", "AO")
                }
            }
            AppLanguage.ENGLISH -> {
                when (cleanCountry) {
                    "GB" -> Locale("en", "GB")
                    "CA" -> Locale("en", "CA")
                    "ZA" -> Locale("en", "ZA")
                    else -> Locale("en", "US")
                }
            }
            AppLanguage.FRENCH -> {
                when (cleanCountry) {
                    "CA" -> Locale("fr", "CA")
                    "CH" -> Locale("fr", "CH")
                    else -> Locale("fr", "FR")
                }
            }
            AppLanguage.SPANISH -> {
                when (cleanCountry) {
                    "ES" -> Locale("es", "ES")
                    else -> Locale("es", "MX")
                }
            }
            else -> Locale(language.code)
        }
    }

    fun formatNumber(value: Double, locale: Locale, decimals: Int = 2): String {
        val nf = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }
        return nf.format(value)
    }

    fun formatPercentage(value: Double, locale: Locale): String {
        val nf = NumberFormat.getPercentInstance(locale).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
        return nf.format(value / 100.0)
    }

    fun formatDate(epochMillis: Long, locale: Locale, timezoneId: String? = null): String {
        val pattern = when (locale.language) {
            "en" -> if (locale.country == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            else -> "dd/MM/yyyy"
        }
        val sdf = SimpleDateFormat(pattern, locale)
        if (!timezoneId.isNullOrBlank()) {
            try {
                sdf.timeZone = TimeZone.getTimeZone(timezoneId)
            } catch (_: Exception) {}
        }
        return sdf.format(Date(epochMillis))
    }

    fun formatDateTime(epochMillis: Long, locale: Locale, timezoneId: String? = null): String {
        val pattern = when (locale.language) {
            "en" -> if (locale.country == "US") "MM/dd/yyyy hh:mm a" else "dd/MM/yyyy HH:mm"
            else -> "dd/MM/yyyy HH:mm"
        }
        val sdf = SimpleDateFormat(pattern, locale)
        if (!timezoneId.isNullOrBlank()) {
            try {
                sdf.timeZone = TimeZone.getTimeZone(timezoneId)
            } catch (_: Exception) {}
        }
        return sdf.format(Date(epochMillis))
    }

    fun formatTime(epochMillis: Long, locale: Locale, timezoneId: String? = null): String {
        val pattern = when (locale.language) {
            "en" -> if (locale.country == "US") "hh:mm a" else "HH:mm"
            else -> "HH:mm"
        }
        val sdf = SimpleDateFormat(pattern, locale)
        if (!timezoneId.isNullOrBlank()) {
            try {
                sdf.timeZone = TimeZone.getTimeZone(timezoneId)
            } catch (_: Exception) {}
        }
        return sdf.format(Date(epochMillis))
    }
}
