package com.example.data.service.fiscal

import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData

enum class FiscalStatus(val label: String) {
    PENDING("Pendente"),
    SUBMITTED("Submetido"),
    ACCEPTED("Aceite"),
    REJECTED("Rejeitado"),
    VALIDATED("Validado"),
    NOT_REQUIRED("Não Requerido"),
    ERROR("Erro de Validação")
}

enum class FiscalEmissionMode(val label: String, val description: String) {
    FISCAL_INTEGRATED("Fiscal Integrado", "Comunicação direta ou validação pela autoridade tributária local"),
    LOCAL_MANUAL("Faturação Local", "Emissão em cumprimento local offline com série certificada"),
    PROFORMA("Pró-forma / Orçamento", "Documento informativo sem valor de cobrança fiscal"),
    TEST("Modo Teste / Sandbox", "Simulação de emissão para testes operacionais")
}

data class FiscalValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

data class FiscalIssueResult(
    val success: Boolean,
    val fiscalValidationHash: String = "",
    val fiscalReference: String = "",
    val qrCodeData: String = "",
    val message: String = ""
)

data class FiscalSubmissionResult(
    val success: Boolean,
    val status: FiscalStatus,
    val reference: String = "",
    val message: String = ""
)

interface FiscalProviderInterface {
    val providerId: String
    val providerName: String
    val countryCode: String
    val taxAuthorityName: String

    fun initialize()
    fun validateInvoice(invoice: InvoiceEntity, items: List<InvoiceItemData>): FiscalValidationResult
    fun issueInvoice(invoice: InvoiceEntity, items: List<InvoiceItemData>): FiscalIssueResult
    fun submitInvoice(invoice: InvoiceEntity): FiscalSubmissionResult
    fun cancelInvoice(invoice: InvoiceEntity, reason: String): FiscalSubmissionResult
    fun getInvoiceStatus(fiscalReference: String): FiscalStatus
    fun generateFiscalQrCode(invoice: InvoiceEntity): String
    fun getFiscalReference(invoice: InvoiceEntity): String
}
