package com.example.data.service.fiscal

import com.example.data.service.fiscal.provider.AngolaAGTProvider
import com.example.data.service.fiscal.provider.BrazilFiscalProvider
import com.example.data.service.fiscal.provider.FranceFiscalProvider
import com.example.data.service.fiscal.provider.GenericInvoiceProvider
import com.example.data.service.fiscal.provider.PortugalATProvider
import com.example.data.service.fiscal.provider.SpainFiscalProvider

class FiscalProviderService {

    private val providers = mutableMapOf<String, FiscalProviderInterface>()

    init {
        registerProvider(AngolaAGTProvider())
        registerProvider(PortugalATProvider())
        registerProvider(BrazilFiscalProvider())
        registerProvider(FranceFiscalProvider())
        registerProvider(SpainFiscalProvider())
    }

    fun registerProvider(provider: FiscalProviderInterface) {
        providers[provider.providerId] = provider
    }

    fun getProviderById(providerId: String): FiscalProviderInterface {
        return providers[providerId] ?: providers["AngolaAGTProvider"] ?: AngolaAGTProvider()
    }

    fun getProviderForCountry(countryCode: String): FiscalProviderInterface {
        val config = CountryFiscalRegistry.getByCode(countryCode)
        return providers[config.defaultProviderId]
            ?: providers.values.find { it.countryCode.equals(countryCode, ignoreCase = true) }
            ?: GenericInvoiceProvider(countryCode = countryCode.uppercase(), taxAuthorityName = config.taxAuthorityName)
    }

    fun getAllProviders(): List<FiscalProviderInterface> {
        return providers.values.toList()
    }
}
