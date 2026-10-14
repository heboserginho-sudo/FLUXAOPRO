package com.example.data.service.fiscal

import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData

data class FormattedInvoiceTemplate(
    val title: String,
    val headerLegalNotice: String,
    val footerLegalNotice: String,
    val taxSummaryHeader: String,
    val taxAuthorityBadgeText: String,
    val qrCodeCaption: String
)

class InvoiceTemplateService {

    fun buildTemplate(
        invoice: InvoiceEntity,
        countryCode: String,
        language: String = "pt"
    ): FormattedInvoiceTemplate {
        val config = CountryFiscalRegistry.getByCode(countryCode)

        val (title, footer, badge) = when (countryCode.uppercase()) {
            "AO" -> Triple(
                "DOCUMENTO EMITIDO PARA FINS FISCAIS - ANGOLA",
                "Os bens ou serviços foram colocados à disposição do adquirente na data do documento. Processado por programa validado n.º 00/AGT/2026 FluxAO.",
                "AUTORIDADE TRIBUTÁRIA: AGT ANGOLA"
            )
            "PT" -> Triple(
                "DOCUMENTO EMITIDO CONFORME LEGISLAÇÃO FISCAL PORTUGUESA",
                "Processado por Programa Certificado n.º 9999/AT/2026 FluxAO. Isenção/Regime de IVA sujeito às disposições do CIVA.",
                "AUTORIDADE TRIBUTÁRIA E ADUANEIRA (AT PORTUGAL)"
            )
            "BR" -> Triple(
                "DANFE SIMPLIFICADO - DOCUMENTO AUXILIAR DA NOTA FISCAL ELETRÔNICA",
                "Documento emitido por ME ou EPP optante pelo Simples Nacional / Regrime de Apuração de Tributos e Impostos no Brasil.",
                "SEFAZ / RECEITA FEDERAL DO BRASIL"
            )
            "FR" -> Triple(
                "FACTURE COMMERCIALE - CONFORME DGFIP FRANCE",
                "Facture établie conformément aux articles 289 et suivants du Code Général des Impôts (CGI France).",
                "DIRECTION GÉNÉRALE DES FINANCES PUBLIQUES (DGFIP)"
            )
            "ES" -> Triple(
                "FACTURA VERIFICABLE - CONFORME AEAT ESPAÑA",
                "Factura expedida cumpliendo los requisitos del Reglamento de Facturación de la Agencia Tributaria Española (AEAT).",
                "AGENCIA TRIBUTARIA (AEAT ESPAÑA)"
            )
            else -> Triple(
                "COMMERCIAL INVOICE / FATURA COMERCIAL",
                "Document issued for commercial and local tax compliance purposes.",
                "TAX AUTHORITY: ${config.taxAuthorityName}"
            )
        }

        return FormattedInvoiceTemplate(
            title = title,
            headerLegalNotice = "Jurisdição Fiscal: ${config.countryName} | Moeda: ${invoice.emitterBank}",
            footerLegalNotice = footer,
            taxSummaryHeader = "Discriminação Fiscal de Impostos (${config.taxAuthorityName})",
            taxAuthorityBadgeText = badge,
            qrCodeCaption = "Validação Digital ${config.taxAuthorityName}"
        )
    }
}
