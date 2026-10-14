package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val country: String,
    val flag: String,
    val locale: Locale,
    val isSymbolPrefix: Boolean = false
) {
    AOA(
        code = "AOA",
        symbol = "Kz",
        displayName = "Kwanza Angolano",
        country = "Angola",
        flag = "🇦🇴",
        locale = Locale("pt", "AO"),
        isSymbolPrefix = true
    ),
    USD(
        code = "USD",
        symbol = "$",
        displayName = "Dólar Americano",
        country = "Estados Unidos",
        flag = "🇺🇸",
        locale = Locale("en", "US"),
        isSymbolPrefix = true
    ),
    EUR(
        code = "EUR",
        symbol = "€",
        displayName = "Euro",
        country = "União Europeia / Portugal",
        flag = "🇪🇺",
        locale = Locale("pt", "PT"),
        isSymbolPrefix = false
    ),
    BRL(
        code = "BRL",
        symbol = "R$",
        displayName = "Real Brasileiro",
        country = "Brasil",
        flag = "🇧🇷",
        locale = Locale("pt", "BR"),
        isSymbolPrefix = true
    ),
    GBP(
        code = "GBP",
        symbol = "£",
        displayName = "Libra Esterlina",
        country = "Reino Unido",
        flag = "🇬🇧",
        locale = Locale("en", "GB"),
        isSymbolPrefix = true
    ),
    MZN(
        code = "MZN",
        symbol = "MT",
        displayName = "Metical Moçambicano",
        country = "Moçambique",
        flag = "🇲🇿",
        locale = Locale("pt", "MZ"),
        isSymbolPrefix = false
    ),
    ZAR(
        code = "ZAR",
        symbol = "R",
        displayName = "Rand Sul-Africano",
        country = "África do Sul",
        flag = "🇿🇦",
        locale = Locale("en", "ZA"),
        isSymbolPrefix = true
    ),
    CAD(
        code = "CAD",
        symbol = "C$",
        displayName = "Dólar Canadense",
        country = "Canadá",
        flag = "🇨🇦",
        locale = Locale("en", "CA"),
        isSymbolPrefix = true
    ),
    CHF(
        code = "CHF",
        symbol = "CHF",
        displayName = "Franco Suíço",
        country = "Suíça",
        flag = "🇨🇭",
        locale = Locale("de", "CH"),
        isSymbolPrefix = true
    ),
    AED(
        code = "AED",
        symbol = "AED",
        displayName = "Dirham dos EAU",
        country = "Emirados Árabes Unidos",
        flag = "🇦🇪",
        locale = Locale("en", "AE"),
        isSymbolPrefix = false
    );

    fun format(value: Double): String {
        val numberFormat = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val formattedNumber = numberFormat.format(value)
        return if (isSymbolPrefix) {
            "$symbol$formattedNumber"
        } else {
            "$formattedNumber $symbol"
        }
    }

    companion object {
        val DEFAULT = AOA

        fun fromCode(code: String?): AppCurrency {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
