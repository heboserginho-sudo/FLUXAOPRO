package com.example.data.i18n

import java.util.TimeZone

data class TimezoneInfo(
    val id: String,
    val displayName: String,
    val utcOffset: String,
    val region: String
)

object TimezoneService {

    val supportedTimezones: List<TimezoneInfo> = listOf(
        TimezoneInfo("Africa/Luanda", "Luanda (Angola)", "UTC+01:00", "África"),
        TimezoneInfo("Europe/Lisbon", "Lisboa (Portugal)", "UTC+00:00", "Europa"),
        TimezoneInfo("America/Sao_Paulo", "São Paulo (Brasil)", "UTC-03:00", "América"),
        TimezoneInfo("America/New_York", "Nova Iorque (EUA)", "UTC-05:00", "América"),
        TimezoneInfo("Europe/Paris", "Paris (França)", "UTC+01:00", "Europa"),
        TimezoneInfo("Europe/Madrid", "Madrid (Espanha)", "UTC+01:00", "Europa"),
        TimezoneInfo("Europe/London", "Londres (Reino Unido)", "UTC+00:00", "Europa"),
        TimezoneInfo("Europe/Berlin", "Berlim (Alemanha)", "UTC+01:00", "Europa"),
        TimezoneInfo("Africa/Maputo", "Maputo (Moçambique)", "UTC+02:00", "África"),
        TimezoneInfo("Atlantic/Cape_Verde", "Praia (Cabo Verde)", "UTC-01:00", "Atlântico"),
        TimezoneInfo("Africa/Sao_Tome", "São Tomé (São Tomé e Príncipe)", "UTC+00:00", "África"),
        TimezoneInfo("America/Toronto", "Toronto (Canadá)", "UTC-05:00", "América"),
        TimezoneInfo("Africa/Johannesburg", "Joanesburgo (África do Sul)", "UTC+02:00", "África"),
        TimezoneInfo("Europe/Zurich", "Zurique (Suíça)", "UTC+01:00", "Europa"),
        TimezoneInfo("Asia/Dubai", "Dubai (Emirados Árabes)", "UTC+04:00", "Oriente Médio"),
        TimezoneInfo("UTC", "Tempo Universal Coordenado (UTC)", "UTC+00:00", "Global")
    )

    fun getTimezone(id: String?): TimezoneInfo {
        if (id.isNullOrBlank()) return supportedTimezones.first()
        return supportedTimezones.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: supportedTimezones.first()
    }

    fun getTimezoneForCountry(countryIsoCode: String): TimezoneInfo {
        val country = CountryDirectory.getByCode(countryIsoCode)
        return getTimezone(country.defaultTimezone)
    }

    fun getDeviceTimezone(): String {
        return TimeZone.getDefault().id
    }
}
