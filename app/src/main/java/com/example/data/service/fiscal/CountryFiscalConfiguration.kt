package com.example.data.service.fiscal

import com.example.data.model.invoice.InvoiceType

data class CountryFiscalConfig(
    val countryCode: String,
    val countryName: String,
    val flagEmoji: String,
    val taxAuthorityName: String,
    val defaultCurrency: String,
    val taxIdLabel: String,
    val defaultProviderId: String,
    val supportedDocumentTypes: List<InvoiceType>,
    val supportedEmissionModes: List<FiscalEmissionMode>,
    val requireFiscalValidation: Boolean,
    val sampleTaxIdFormat: String
)

object CountryFiscalRegistry {

    val ANGOLA = CountryFiscalConfig(
        countryCode = "AO",
        countryName = "Angola",
        flagEmoji = "🇦🇴",
        taxAuthorityName = "AGT",
        defaultCurrency = "AOA",
        taxIdLabel = "NIF",
        defaultProviderId = "AngolaAGTProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.FISCAL_INTEGRATED,
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = true,
        sampleTaxIdFormat = "5412345678"
    )

    val PORTUGAL = CountryFiscalConfig(
        countryCode = "PT",
        countryName = "Portugal",
        flagEmoji = "🇵🇹",
        taxAuthorityName = "AT",
        defaultCurrency = "EUR",
        taxIdLabel = "NIF / NIPC",
        defaultProviderId = "PortugalATProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.FISCAL_INTEGRATED,
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = true,
        sampleTaxIdFormat = "123456789"
    )

    val BRAZIL = CountryFiscalConfig(
        countryCode = "BR",
        countryName = "Brasil",
        flagEmoji = "🇧🇷",
        taxAuthorityName = "SEFAZ / Receita Federal",
        defaultCurrency = "BRL",
        taxIdLabel = "CNPJ / CPF",
        defaultProviderId = "BrazilFiscalProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.FISCAL_INTEGRATED,
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = true,
        sampleTaxIdFormat = "12.345.678/0001-90"
    )

    val FRANCE = CountryFiscalConfig(
        countryCode = "FR",
        countryName = "França",
        flagEmoji = "🇫🇷",
        taxAuthorityName = "DGFiP",
        defaultCurrency = "EUR",
        taxIdLabel = "SIRET / TVA Intracommunautaire",
        defaultProviderId = "FranceFiscalProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.FISCAL_INTEGRATED,
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = false,
        sampleTaxIdFormat = "FR12345678901"
    )

    val SPAIN = CountryFiscalConfig(
        countryCode = "ES",
        countryName = "Espanha",
        flagEmoji = "🇪🇸",
        taxAuthorityName = "AEAT / Veri*Factu",
        defaultCurrency = "EUR",
        taxIdLabel = "NIF / NIE / CIF",
        defaultProviderId = "SpainFiscalProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.FISCAL_INTEGRATED,
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = true,
        sampleTaxIdFormat = "B12345678"
    )

    val UNITED_STATES = CountryFiscalConfig(
        countryCode = "US",
        countryName = "Estados Unidos",
        flagEmoji = "🇺🇸",
        taxAuthorityName = "IRS / State Dept of Revenue",
        defaultCurrency = "USD",
        taxIdLabel = "EIN / SSN",
        defaultProviderId = "GenericInvoiceProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = false,
        sampleTaxIdFormat = "12-3456789"
    )

    val UNITED_KINGDOM = CountryFiscalConfig(
        countryCode = "GB",
        countryName = "Reino Unido",
        flagEmoji = "🇬🇧",
        taxAuthorityName = "HMRC",
        defaultCurrency = "GBP",
        taxIdLabel = "VAT Reg No",
        defaultProviderId = "GenericInvoiceProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = false,
        sampleTaxIdFormat = "GB123456789"
    )

    val MOZAMBIQUE = CountryFiscalConfig(
        countryCode = "MZ",
        countryName = "Moçambique",
        flagEmoji = "🇲🇿",
        taxAuthorityName = "AT Moçambique",
        defaultCurrency = "MZN",
        taxIdLabel = "NUIT",
        defaultProviderId = "GenericInvoiceProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = false,
        sampleTaxIdFormat = "123456789"
    )

    val CAPE_VERDE = CountryFiscalConfig(
        countryCode = "CV",
        countryName = "Cabo Verde",
        flagEmoji = "🇨🇻",
        taxAuthorityName = "DNRE",
        defaultCurrency = "CVE",
        taxIdLabel = "NIF",
        defaultProviderId = "GenericInvoiceProvider",
        supportedDocumentTypes = listOf(InvoiceType.FT, InvoiceType.FR, InvoiceType.FP, InvoiceType.NC),
        supportedEmissionModes = listOf(
            FiscalEmissionMode.LOCAL_MANUAL,
            FiscalEmissionMode.PROFORMA,
            FiscalEmissionMode.TEST
        ),
        requireFiscalValidation = false,
        sampleTaxIdFormat = "123456789"
    )

    val allCountries: List<CountryFiscalConfig> = listOf(
        ANGOLA, PORTUGAL, BRAZIL, FRANCE, SPAIN, UNITED_STATES, UNITED_KINGDOM, MOZAMBIQUE, CAPE_VERDE
    )

    fun getByCode(code: String): CountryFiscalConfig {
        return allCountries.find { it.countryCode.equals(code, ignoreCase = true) } ?: ANGOLA
    }
}
