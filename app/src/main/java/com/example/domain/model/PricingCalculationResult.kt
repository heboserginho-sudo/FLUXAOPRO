package com.example.domain.model

import com.example.data.model.BreakEvenStatus

data class PricingCalculationResult(
    val directMaterialsCost: Double,
    val directLaborCost: Double,
    val totalDirectCost: Double,
    val overheadPercentage: Double,
    val overheadCost: Double,
    val totalProductionCost: Double,
    val markupDivisor: Double,
    val markupMultiplier: Double,
    val basePrice: Double,
    val discountAmount: Double,
    val finalSellingPrice: Double,
    val taxesPercentage: Double,
    val taxesAmount: Double,
    val netProfit: Double,
    val netProfitMarginPercent: Double,
    val contributionMargin: Double,
    val contributionMarginPercent: Double,
    val breakEvenPrice: Double,
    val breakEvenStatus: BreakEvenStatus,
    val breakEvenAnalysisText: String,
    // Metodologia Angolana de Orçamento
    val laborPercentageOnMaterials: Double = 85.0,
    val profitPercentageOnCost: Double = 80.0,
    val subtotalWithProfit: Double = 0.0,
    val costBaseWithLabor: Double = 0.0
)
