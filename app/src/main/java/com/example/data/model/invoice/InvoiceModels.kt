package com.example.data.model.invoice

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InvoiceType(
    val code: String,
    val title: String,
    val shortName: String,
    val description: String
) {
    FT("FT", "Fatura Comercial", "Fatura", "Documento para cobrança a pronto ou a prazo"),
    FR("FR", "Fatura-Recibo", "Fatura-Recibo", "Comprovativo simultâneo de venda e liquidação"),
    FP("FP", "Fatura Pró-forma", "Pró-forma", "Cotação / Proposta orçamental informativa sem valor contabilístico"),
    NC("NC", "Nota de Crédito", "Nota Crédito", "Retificação ou anulação de fatura emitida")
}

enum class InvoiceStatus(
    val title: String,
    val colorHex: Long
) {
    PENDING("Pendente", 0xFFF59E0B),
    PAID("Liquidada", 0xFF10B981),
    CANCELLED("Anulada", 0xFFEF4444)
}

enum class AngolaVatRegime(
    val title: String,
    val defaultRate: Double
) {
    GERAL("Regime Geral (IVA 14%)", 14.0),
    SIMPLIFICADO("Regime Simplificado (IVA 7%)", 7.0),
    EXCLUSAO("Regime de Exclusão (Isento 0%)", 0.0)
}

enum class AngolaPaymentMethod(
    val title: String,
    val iconName: String
) {
    MULTICAIXA_EXPRESS("Multicaixa Express", "mcx"),
    TRANSFERENCIA("Transferência Bancária / IBAN", "bank"),
    NUMERARIO("Numerário (Kwanza em mão)", "cash"),
    TPA("TPA / Cartão Débito", "card"),
    PRAZO_30("A Prazo (30 Dias)", "calendar"),
    PRAZO_60("A Prazo (60 Dias)", "calendar")
}

data class InvoiceItemData(
    val id: Long = 0,
    val description: String = "",
    val unit: String = "Un",
    val quantity: Double = 1.0,
    val unitPriceKz: Double = 0.0,
    val discountPercent: Double = 0.0,
    val vatRate: Double = 14.0,
    val exemptionCode: String = ""
) {
    val grossTotal: Double get() = quantity * unitPriceKz
    val discountAmount: Double get() = grossTotal * (discountPercent / 100.0)
    val netTotal: Double get() = grossTotal - discountAmount
    val vatAmount: Double get() = netTotal * (vatRate / 100.0)
    val finalTotal: Double get() = netTotal + vatAmount
}

data class InvoiceSummary(
    val totalGross: Double,
    val totalDiscount: Double,
    val totalNet: Double,
    val totalVat: Double,
    val totalToPay: Double
)
