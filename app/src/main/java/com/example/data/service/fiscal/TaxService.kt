package com.example.data.service.fiscal

data class TaxConfiguration(
    val countryCode: String,
    val taxCode: String,
    val taxName: String,
    val taxRatePercent: Double,
    val isDefault: Boolean = false,
    val exemptionRequiredIfZero: Boolean = true,
    val description: String = ""
)

class TaxService {

    private val taxRegistry = mutableMapOf<String, List<TaxConfiguration>>()

    init {
        // ANGOLA
        taxRegistry["AO"] = listOf(
            TaxConfiguration("AO", "NOR", "IVA Geral 14%", 14.0, isDefault = true, description = "Regime Geral de IVA em Angola"),
            TaxConfiguration("AO", "RED", "IVA Reduzido 7%", 7.0, description = "Bens alimentares e regime simplificado"),
            TaxConfiguration("AO", "ISE", "Isento (IVA 0%)", 0.0, exemptionRequiredIfZero = true, description = "Sujeito a código de isenção AGT"),
            TaxConfiguration("AO", "IS", "Imposto de Selo 1%", 1.0, description = "Recibos e verbas de selo")
        )

        // PORTUGAL
        taxRegistry["PT"] = listOf(
            TaxConfiguration("PT", "NOR", "IVA Normal 23%", 23.0, isDefault = true, description = "Taxa normal Continente"),
            TaxConfiguration("PT", "INT", "IVA Intermédio 13%", 13.0, description = "Restauração e bens alimentares"),
            TaxConfiguration("PT", "RED", "IVA Reduzido 6%", 6.0, description = "Produtos farmacêuticos, livros e eletricidade"),
            TaxConfiguration("PT", "ISE", "IVA Isento 0%", 0.0, exemptionRequiredIfZero = true, description = "Isenção Artigo 9.º ou 53.º CIVA")
        )

        // BRASIL
        taxRegistry["BR"] = listOf(
            TaxConfiguration("BR", "ISS", "ISS Municipal 5%", 5.0, isDefault = true, description = "Imposto Sobre Serviços de Qualquer Natureza"),
            TaxConfiguration("BR", "ICMS", "ICMS Estadual 18%", 18.0, description = "Imposto sobre Circulação de Mercadorias"),
            TaxConfiguration("BR", "SIMPLES", "Simples Nacional 6%", 6.0, description = "Alíquota unificada de microempresa"),
            TaxConfiguration("BR", "PIS_COFINS", "PIS/COFINS 3.65%", 3.65, description = "Contribuições federais sobre faturamento")
        )

        // FRANÇA
        taxRegistry["FR"] = listOf(
            TaxConfiguration("FR", "NOR", "TVA Normal 20%", 20.0, isDefault = true, description = "Taux normal France métropolitaine"),
            TaxConfiguration("FR", "INT", "TVA Intermédiaire 10%", 10.0, description = "Restauration, travaux de rénovation"),
            TaxConfiguration("FR", "RED", "TVA Réduit 5.5%", 5.5, description = "Produits alimentaires, livres"),
            TaxConfiguration("FR", "EXO", "TVA Exonérée 0%", 0.0, exemptionRequiredIfZero = true, description = "Franchise en base de TVA")
        )

        // ESPANHA
        taxRegistry["ES"] = listOf(
            TaxConfiguration("ES", "NOR", "IVA General 21%", 21.0, isDefault = true, description = "Tipo general de IVA en España"),
            TaxConfiguration("ES", "RED", "IVA Reducido 10%", 10.0, description = "Hostelería, transporte de viajeros"),
            TaxConfiguration("ES", "SRED", "IVA Superreducido 4%", 4.0, description = "Pan, leche, libros, medicamentos"),
            TaxConfiguration("ES", "EXO", "Exento de IVA 0%", 0.0, exemptionRequiredIfZero = true, description = "Operaciones exentas Art. 20 LIVA")
        )

        // ESTADOS UNIDOS / OUTROS
        taxRegistry["US"] = listOf(
            TaxConfiguration("US", "TAX_7", "State Sales Tax 7%", 7.0, isDefault = true, description = "Standard State Sales Tax"),
            TaxConfiguration("US", "TAX_0", "Zero Sales Tax 0%", 0.0, description = "Exempt / Wholesale")
        )
    }

    fun getTaxesForCountry(countryCode: String): List<TaxConfiguration> {
        return taxRegistry[countryCode.uppercase()]
            ?: taxRegistry["AO"]
            ?: emptyList()
    }

    fun getDefaultTaxForCountry(countryCode: String): TaxConfiguration {
        val taxes = getTaxesForCountry(countryCode)
        return taxes.find { it.isDefault } ?: taxes.firstOrNull() ?: TaxConfiguration("AO", "NOR", "IVA 14%", 14.0)
    }

    fun calculateTaxAmount(netTotal: Double, taxRatePercent: Double): Double {
        return netTotal * (taxRatePercent / 100.0)
    }
}
