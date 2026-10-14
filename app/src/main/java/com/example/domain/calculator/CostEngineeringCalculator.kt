package com.example.domain.calculator

import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.model.AppCurrency
import com.example.data.model.BreakEvenStatus
import com.example.domain.model.PricingCalculationResult
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object CostEngineeringCalculator {

    var currentCurrency: AppCurrency = AppCurrency.AOA

    private val ptBrLocale = Locale("pt", "BR")
    private val percentFormat = NumberFormat.getNumberInstance(ptBrLocale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }

    fun setCurrency(currency: AppCurrency) {
        currentCurrency = currency
    }

    fun formatCurrency(value: Double, currency: AppCurrency = currentCurrency): String {
        return currency.format(value)
    }

    fun formatPercent(value: Double): String = "${percentFormat.format(value)}%"

    fun calculate(
        materialsCost: Double,
        laborHours: Double = 0.0,
        laborHourlyRate: Double = 0.0,
        overheadPercentage: Double = 0.0,
        taxesPercent: Double = 14.0, // Imposto IVA AGT padrão de Angola (14%)
        desiredProfitMarginPercent: Double = 80.0, // Lucro Líquido padrão no Método Angolano (70% a 100% ou mais)
        discountAmount: Double = 0.0,
        currency: AppCurrency = currentCurrency,
        laborPercentageOnMaterials: Double = 85.0, // Mão de obra = 85% do valor da matéria-prima
        useAngolanMethod: Boolean = true
    ): PricingCalculationResult {
        val directMaterials = max(0.0, materialsCost)

        // METODOLOGIA ANGOLANA:
        // Mão de Obra = 85% do valor em matéria-prima utilizada!
        // Se houver matérias-primas cadastradas, a mão de obra é 85% da matéria-prima.
        // Se for serviço puro (sem matéria-prima), utiliza horas de mão de obra se informadas.
        val directLabor = if (useAngolanMethod) {
            if (directMaterials > 0.0) {
                directMaterials * (max(0.0, laborPercentageOnMaterials) / 100.0)
            } else {
                max(0.0, laborHours * laborHourlyRate)
            }
        } else {
            max(0.0, laborHours * laborHourlyRate)
        }

        // Custo Direto Total = Matéria-prima + Mão de Obra (85%)
        val totalDirect = directMaterials + directLabor

        // Custos Indiretos / Rateio Operacional (se houver)
        val overheadCost = totalDirect * (max(0.0, overheadPercentage) / 100.0)
        val totalProductionCost = totalDirect + overheadCost

        val finalPrice: Double
        val basePrice: Double
        val taxesAmount: Double
        val netProfit: Double
        val subtotalWithProfit: Double
        val markupMultiplier: Double
        val markupDivisor: Double

        if (useAngolanMethod) {
            // METODOLOGIA ANGOLANA DE PRECIFICAÇÃO:
            // 1. Custo Base = Matéria-prima + Mão de Obra (85%) + Custos Indiretos
            // 2. Lucro Líquido = Custo Base * (Margem de Lucro % / 100) -> 70% a 100%+
            // 3. Subtotal com Lucro = Custo Base + Lucro Líquido
            // 4. Imposto IVA AGT (14%) = Subtotal com Lucro * (14 / 100)
            // 5. Preço Final = Subtotal com Lucro + Imposto IVA - Desconto
            val profitRate = max(0.0, desiredProfitMarginPercent) / 100.0
            val profitAmount = totalProductionCost * profitRate
            subtotalWithProfit = totalProductionCost + profitAmount

            val taxRate = max(0.0, taxesPercent) / 100.0
            taxesAmount = subtotalWithProfit * taxRate
            basePrice = subtotalWithProfit + taxesAmount
            finalPrice = max(0.0, basePrice - discountAmount)

            // Lucro Líquido Real obtido no bolso após pagar matéria-prima, mão de obra (85%) e IVA AGT (14%)
            netProfit = max(0.0, finalPrice - taxesAmount - totalProductionCost)
            markupMultiplier = if (totalProductionCost > 0.0) finalPrice / totalProductionCost else 1.0
            markupDivisor = if (finalPrice > 0.0) totalProductionCost / finalPrice else 1.0
        } else {
            // Fallback Markup Divisor tradicional
            val deductionsPercent = (taxesPercent + desiredProfitMarginPercent).coerceAtMost(90.0)
            val divisorRaw = 1.0 - (deductionsPercent / 100.0)
            markupDivisor = max(0.05, divisorRaw)
            markupMultiplier = if (markupDivisor > 0) 1.0 / markupDivisor else 1.0
            basePrice = if (markupDivisor > 0) totalProductionCost / markupDivisor else totalProductionCost
            finalPrice = max(0.0, basePrice - discountAmount)
            taxesAmount = finalPrice * (taxesPercent / 100.0)
            netProfit = finalPrice - totalProductionCost - taxesAmount
            subtotalWithProfit = finalPrice - taxesAmount
        }

        // Margem de Lucro Líquido sobre o Custo de Produção (70% a 100%+)
        val netProfitMarginPercent = if (totalProductionCost > 0.0) {
            (netProfit / totalProductionCost) * 100.0
        } else if (finalPrice > 0.0) {
            (netProfit / finalPrice) * 100.0
        } else {
            0.0
        }

        val contributionMargin = finalPrice - totalDirect - taxesAmount
        val contributionMarginPercent = if (finalPrice > 0.0) (contributionMargin / finalPrice) * 100.0 else 0.0

        // Ponto de Equilíbrio (Break-Even Price): Cobre matéria-prima + MDO (85%) + IVA AGT (14%)
        val breakEvenPrice = totalProductionCost * (1.0 + (taxesPercent / 100.0))

        val breakEvenStatus: BreakEvenStatus
        val analysisText: String

        when {
            finalPrice < breakEvenPrice -> {
                val deficit = breakEvenPrice - finalPrice
                breakEvenStatus = BreakEvenStatus.DEFICIT
                analysisText = "Prejuízo operacional projetado de ${formatCurrency(deficit, currency)}. O preço cobrado não cobre as matérias-primas, mão de obra (85%) e impostos fiscais."
            }
            netProfitMarginPercent >= 100.0 -> {
                breakEvenStatus = BreakEvenStatus.HEALTHY
                analysisText = "Excelente rentabilidade no Método Angolano! Lucro líquido de ${formatPercent(netProfitMarginPercent)} (${formatCurrency(netProfit, currency)}), cobrindo matérias-primas, mão de obra (${formatPercent(laborPercentageOnMaterials)}) e IVA AGT (${formatPercent(taxesPercent)})."
            }
            netProfitMarginPercent >= 70.0 -> {
                breakEvenStatus = BreakEvenStatus.HEALTHY
                analysisText = "Meta atingida pelo Método Angolano! Lucro líquido saudável de ${formatPercent(netProfitMarginPercent)} (${formatCurrency(netProfit, currency)}), com garantia total sobre custos de matérias-primas e mão de obra de 85%."
            }
            netProfitMarginPercent > 0.0 -> {
                breakEvenStatus = BreakEvenStatus.VIABLE
                analysisText = "Orçamento viável com lucro líquido de ${formatPercent(netProfitMarginPercent)} (${formatCurrency(netProfit, currency)}). Para atingir a meta padrão em Angola, eleve a margem para entre 70% e 100%."
            }
            else -> {
                breakEvenStatus = BreakEvenStatus.WARNING
                analysisText = "Atenção: margem nula ou reduzida após descontos. Ponto de equilíbrio em ${formatCurrency(breakEvenPrice, currency)}."
            }
        }

        return PricingCalculationResult(
            directMaterialsCost = directMaterials,
            directLaborCost = directLabor,
            totalDirectCost = totalDirect,
            overheadPercentage = overheadPercentage,
            overheadCost = overheadCost,
            totalProductionCost = totalProductionCost,
            markupDivisor = markupDivisor,
            markupMultiplier = markupMultiplier,
            basePrice = basePrice,
            discountAmount = discountAmount,
            finalSellingPrice = finalPrice,
            taxesPercentage = taxesPercent,
            taxesAmount = taxesAmount,
            netProfit = netProfit,
            netProfitMarginPercent = netProfitMarginPercent,
            contributionMargin = contributionMargin,
            contributionMarginPercent = contributionMarginPercent,
            breakEvenPrice = breakEvenPrice,
            breakEvenStatus = breakEvenStatus,
            breakEvenAnalysisText = analysisText,
            laborPercentageOnMaterials = laborPercentageOnMaterials,
            profitPercentageOnCost = desiredProfitMarginPercent,
            subtotalWithProfit = subtotalWithProfit,
            costBaseWithLabor = totalDirect
        )
    }

    fun calculateForBudget(budget: BudgetEntity): PricingCalculationResult {
        return calculate(
            materialsCost = budget.directMaterialsTotal,
            laborHours = budget.laborHours,
            laborHourlyRate = budget.laborHourlyRate,
            overheadPercentage = budget.overheadPercentage,
            taxesPercent = budget.taxesAndSalesFeePercent,
            desiredProfitMarginPercent = budget.desiredProfitMarginPercent,
            discountAmount = budget.discountAmount,
            useAngolanMethod = true,
            laborPercentageOnMaterials = budget.laborPercentageOnMaterials
        )
    }

    fun generateExecutiveProposalText(
        budget: BudgetEntity,
        items: List<BudgetItemEntity>,
        calculation: PricingCalculationResult,
        currency: AppCurrency = currentCurrency
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", currency.locale)
        val dateStr = dateFormat.format(Date(budget.dateCreatedEpochMillis))

        val builder = StringBuilder()
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("💼 PROPOSTA COMERCIAL & ORÇAMENTO EXECUTIVO\n")
        builder.append("   (Metodologia de Custos de Angola)\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
        builder.append("📋 PROJETO: ${budget.projectTitle}\n")
        builder.append("👤 CLIENTE: ${budget.clientName}\n")
        if (budget.clientContact.isNotBlank()) builder.append("📱 CONTATO: ${budget.clientContact}\n")
        builder.append("📅 DATA DE EMISSÃO: $dateStr\n")
        builder.append("⏱ PRAZO DE EXECUÇÃO: ${budget.deadlineDays} dias úteis\n\n")

        if (budget.projectScope.isNotBlank()) {
            builder.append("🎯 ESCOPO DOS SERVIÇOS:\n${budget.projectScope.trim()}\n\n")
        }

        if (items.isNotEmpty()) {
            builder.append("📦 MATÉRIAS-PRIMAS & INSUMOS UTILIZADOS:\n")
            items.forEachIndexed { index, item ->
                builder.append("  ${index + 1}. ${item.description} (${item.quantity} un x ${formatCurrency(item.unitPrice, currency)}) = ${formatCurrency(item.total, currency)}\n")
            }
            builder.append("  ▶ Subtotal Matérias-Primas: ${formatCurrency(calculation.directMaterialsCost, currency)}\n\n")
        }

        builder.append("⚙️ EXECUÇÃO & MÃO DE OBRA:\n")
        builder.append("  • Mão de Obra Técnica Especializada (${formatPercent(calculation.laborPercentageOnMaterials)}): ${formatCurrency(calculation.directLaborCost, currency)}\n")
        builder.append("  • Custo Base de Produção: ${formatCurrency(calculation.totalProductionCost, currency)}\n\n")

        builder.append("💰 RESUMO FINANCEIRO (MÉTODO ANGOLANO):\n")
        builder.append("  • Custo Base (Matéria-prima + MDO 85%): ${formatCurrency(calculation.totalProductionCost, currency)}\n")
        builder.append("  • Margem de Lucro Garantido (${formatPercent(calculation.profitPercentageOnCost)}): +${formatCurrency(calculation.netProfit, currency)}\n")
        builder.append("  • Imposto IVA AGT (${formatPercent(calculation.taxesPercentage)}): +${formatCurrency(calculation.taxesAmount, currency)}\n")
        if (calculation.discountAmount > 0) {
            builder.append("  • Desconto Comercial: -${formatCurrency(calculation.discountAmount, currency)}\n")
        }
        builder.append("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("  ★ VALOR TOTAL DO ORÇAMENTO: ${formatCurrency(calculation.finalSellingPrice, currency)}\n\n")

        builder.append("💳 FORMA DE PAGAMENTO:\n  ${budget.paymentTerms}\n\n")
        builder.append("📜 TERMOS E CONDIÇÕES:\n  ${budget.termsAndConditions}\n\n")
        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        builder.append("Moeda: ${currency.displayName} (${currency.symbol} - ${currency.code})\n")
        builder.append("Orçamento elaborado conforme metodologia técnica e comercial de Angola.")
        return builder.toString()
    }
}
