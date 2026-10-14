package com.example.data.service.fiscal

import com.example.data.model.AppCurrency

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flagEmoji: String,
    val exchangeRateToBaseUsd: Double = 1.0
)

data class ConvertedAmount(
    val originalAmount: Double,
    val originalCurrency: String,
    val convertedAmount: Double,
    val targetCurrency: String,
    val exchangeRate: Double
)

class CurrencyService {

    private val currencies = mapOf(
        "AOA" to CurrencyInfo("AOA", "Kwanza Angolano", "Kz", "🇦🇴", 0.0011),
        "EUR" to CurrencyInfo("EUR", "Euro", "€", "🇪🇺", 1.09),
        "BRL" to CurrencyInfo("BRL", "Real Brasileiro", "R$", "🇧🇷", 0.18),
        "USD" to CurrencyInfo("USD", "Dólar Americano", "$", "🇺🇸", 1.00),
        "GBP" to CurrencyInfo("GBP", "Libra Esterlina", "£", "🇬🇧", 1.28),
        "MZN" to CurrencyInfo("MZN", "Metical Moçambicano", "MT", "🇲🇿", 0.016),
        "CVE" to CurrencyInfo("CVE", "Escudo Cabo-Verdiano", "PTE", "🇨🇻", 0.0098),
        "STN" to CurrencyInfo("STN", "Dobra de São Tomé", "Db", "🇸🇹", 0.044)
    )

    fun getCurrencyInfo(code: String): CurrencyInfo {
        return currencies[code.uppercase()] ?: CurrencyInfo(code, code, code, "🌐", 1.0)
    }

    fun getAllCurrencies(): List<CurrencyInfo> {
        return currencies.values.toList()
    }

    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): ConvertedAmount {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) {
            return ConvertedAmount(amount, fromCurrency, amount, toCurrency, 1.0)
        }

        val fromInfo = getCurrencyInfo(fromCurrency)
        val toInfo = getCurrencyInfo(toCurrency)

        val amountInUsd = amount * fromInfo.exchangeRateToBaseUsd
        val convertedAmount = amountInUsd / toInfo.exchangeRateToBaseUsd
        val effectiveRate = if (amount != 0.0) convertedAmount / amount else 1.0

        return ConvertedAmount(
            originalAmount = amount,
            originalCurrency = fromCurrency,
            convertedAmount = convertedAmount,
            targetCurrency = toCurrency,
            exchangeRate = effectiveRate
        )
    }

    fun formatAmountWithCurrency(amount: Double, currencyCode: String): String {
        val info = getCurrencyInfo(currencyCode)
        return "%,.2f %s".format(amount, info.symbol)
    }
}
