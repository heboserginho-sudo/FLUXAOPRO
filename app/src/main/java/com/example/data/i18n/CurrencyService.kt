package com.example.data.i18n

import com.example.data.model.AppCurrency
import java.text.NumberFormat
import java.util.Locale

object CurrencyService {

    // Base exchange rate pegged to 1 USD
    // AOA: 925.0
    // EUR: 0.92
    // GBP: 0.79
    // BRL: 5.45
    // MZN: 63.8
    // ZAR: 18.2
    // CAD: 1.36
    // CHF: 0.89
    // AED: 3.67
    private val usdRates: Map<String, Double> = mapOf(
        "USD" to 1.0,
        "AOA" to 925.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "BRL" to 5.45,
        "MZN" to 63.8,
        "ZAR" to 18.2,
        "CAD" to 1.36,
        "CHF" to 0.89,
        "AED" to 3.67
    )

    fun getExchangeRate(from: AppCurrency, to: AppCurrency): Double {
        if (from == to) return 1.0
        val fromUsd = usdRates[from.code] ?: 1.0
        val toUsd = usdRates[to.code] ?: 1.0
        // rate = (1 / fromUsd) * toUsd
        return toUsd / fromUsd
    }

    fun convert(amount: Double, from: AppCurrency, to: AppCurrency): Double {
        val rate = getExchangeRate(from, to)
        return amount * rate
    }

    fun format(value: Double, currency: AppCurrency, customLocale: Locale? = null): String {
        val targetLocale = customLocale ?: currency.locale
        val nf = NumberFormat.getNumberInstance(targetLocale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val formattedNumber = nf.format(value)
        return if (currency.isSymbolPrefix) {
            "${currency.symbol}$formattedNumber"
        } else {
            "$formattedNumber ${currency.symbol}"
        }
    }

    fun formatDual(
        amount: Double,
        baseCurrency: AppCurrency,
        secondaryCurrency: AppCurrency
    ): String {
        val baseStr = format(amount, baseCurrency)
        if (baseCurrency == secondaryCurrency) return baseStr
        val converted = convert(amount, baseCurrency, secondaryCurrency)
        val convStr = format(converted, secondaryCurrency)
        return "$baseStr ($convStr)"
    }

    fun getCurrencyForCountry(countryIsoCode: String): AppCurrency {
        val country = CountryDirectory.getByCode(countryIsoCode)
        return AppCurrency.fromCode(country.defaultCurrency)
    }

    fun getAllRatesTable(): List<ExchangeRateItem> {
        val base = AppCurrency.USD
        return AppCurrency.values().map { curr ->
            val rateFromUsd = getExchangeRate(base, curr)
            val rateToAoa = getExchangeRate(curr, AppCurrency.AOA)
            ExchangeRateItem(
                currency = curr,
                ratePerUsd = rateFromUsd,
                rateInAoa = rateToAoa
            )
        }
    }
}

data class ExchangeRateItem(
    val currency: AppCurrency,
    val ratePerUsd: Double,
    val rateInAoa: Double
)
