package com.example.data.i18n

data class PhoneParseResult(
    val countryCallingCode: String,
    val nationalNumber: String,
    val e164Number: String,
    val isValid: Boolean,
    val countryInfo: CountryInfo?
)

object InternationalPhoneHelper {

    fun formatToE164(callingCode: String, nationalNumber: String): String {
        val cleanCode = if (callingCode.startsWith("+")) callingCode else "+$callingCode"
        val cleanNational = nationalNumber.replace("[^0-9]".toRegex(), "").trimStart('0')
        return "$cleanCode$cleanNational"
    }

    fun parsePhone(rawPhone: String, defaultCountryIso: String = "AO"): PhoneParseResult {
        val trimmed = rawPhone.trim()
        val digitsOnly = trimmed.replace("[^0-9+]".toRegex(), "")

        // Check if starts with a known calling code
        for (country in CountryDirectory.allCountries) {
            val code = country.callingCode // e.g. +244
            if (digitsOnly.startsWith(code)) {
                val national = digitsOnly.substring(code.length).trimStart('0')
                val e164 = "$code$national"
                val isValid = national.length in 7..12
                return PhoneParseResult(
                    countryCallingCode = code,
                    nationalNumber = national,
                    e164Number = e164,
                    isValid = isValid,
                    countryInfo = country
                )
            }
        }

        // If no calling code prefix, assign default country
        val defaultCountry = CountryDirectory.getByCode(defaultCountryIso)
        val national = digitsOnly.replace("+", "").trimStart('0')
        val e164 = "${defaultCountry.callingCode}$national"
        val isValid = national.length in 7..12

        return PhoneParseResult(
            countryCallingCode = defaultCountry.callingCode,
            nationalNumber = national,
            e164Number = e164,
            isValid = isValid,
            countryInfo = defaultCountry
        )
    }

    fun getCallingCodes(): List<Pair<CountryInfo, String>> {
        return CountryDirectory.allCountries.map { it to it.callingCode }
    }
}
