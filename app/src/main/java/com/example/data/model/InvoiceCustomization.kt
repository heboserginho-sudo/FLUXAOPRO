package com.example.data.model

import androidx.compose.ui.graphics.Color

data class InvoiceCustomization(
    val logoBase64: String = "",
    val logoLocalPath: String = "",
    val accentColorHex: String = "#D4AF37",
    val templateStyle: String = "MODERN",
    val customFooterNotes: String = "Obrigado pela preferência! Documento processado por computador nos termos da legislação fiscal da República de Angola.",
    val showLogoOnInvoice: Boolean = true,
    val showWatermark: Boolean = true,
    val showQrCode: Boolean = true,
    val bankAccountInfo: String = "BFA • IBAN: 0006 0000 43818047301 44"
) {
    val composeAccentColor: Color
        get() = try {
            val clean = accentColorHex.removePrefix("#")
            val colorLong = clean.toLong(16)
            if (clean.length == 6) Color(0xFF000000 or colorLong) else Color(colorLong)
        } catch (e: Exception) {
            Color(0xFFD4AF37)
        }

    val styleDisplayName: String
        get() = when (templateStyle) {
            "CLASSIC" -> "Clássico Centrado"
            "MINIMAL" -> "Elegante Minimalista"
            else -> "Moderno Executivo"
        }
}

val PRESET_INVOICE_COLORS = listOf(
    Pair("#D4AF37", "Ouro Executivo"),
    Pair("#1E3A8A", "Azul Corporativo"),
    Pair("#047857", "Verde Esmeralda"),
    Pair("#991B1B", "Vermelho Bordô"),
    Pair("#334155", "Grafite Moderno"),
    Pair("#7C3AED", "Roxo Premium")
)
