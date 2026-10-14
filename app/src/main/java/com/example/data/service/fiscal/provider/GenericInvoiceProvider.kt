package com.example.data.service.fiscal.provider

import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.service.fiscal.FiscalIssueResult
import com.example.data.service.fiscal.FiscalProviderInterface
import com.example.data.service.fiscal.FiscalStatus
import com.example.data.service.fiscal.FiscalSubmissionResult
import com.example.data.service.fiscal.FiscalValidationResult
import java.security.MessageDigest

class GenericInvoiceProvider(
    override val countryCode: String = "US",
    override val taxAuthorityName: String = "Local Tax Authority"
) : FiscalProviderInterface {

    override val providerId: String = "GenericInvoiceProvider_$countryCode"
    override val providerName: String = "Emissão Comercial / Faturação Local ($countryCode)"

    override fun initialize() {}

    override fun validateInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): FiscalValidationResult {
        val errors = mutableListOf<String>()
        if (items.isEmpty()) {
            errors.add("Adicione pelo menos 1 item na fatura.")
        }
        return FiscalValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    override fun issueInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): FiscalIssueResult {
        val ref = getFiscalReference(invoice)
        val hash = generateHash("${invoice.invoiceNumber}-${invoice.totalToPay}")
        return FiscalIssueResult(
            success = true,
            fiscalValidationHash = hash,
            fiscalReference = ref,
            qrCodeData = generateFiscalQrCode(invoice),
            message = "Documento emitido no modo de Faturação Local conforme normas fiscais de $countryCode."
        )
    }

    override fun submitInvoice(invoice: InvoiceEntity): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.NOT_REQUIRED,
            reference = getFiscalReference(invoice),
            message = "Documento comercial local não requer submissão prévia no país $countryCode."
        )
    }

    override fun cancelInvoice(invoice: InvoiceEntity, reason: String): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.VALIDATED,
            reference = "CANCEL-${getFiscalReference(invoice)}",
            message = "Documento anulado localmente com sucesso."
        )
    }

    override fun getInvoiceStatus(fiscalReference: String): FiscalStatus = FiscalStatus.NOT_REQUIRED

    override fun generateFiscalQrCode(invoice: InvoiceEntity): String =
        "https://fluxao.app/verify-invoice?doc=${invoice.invoiceNumber}&country=$countryCode&total=${invoice.totalToPay}"

    override fun getFiscalReference(invoice: InvoiceEntity): String =
        "GEN-$countryCode-${invoice.invoiceNumber.replace(" ", "")}"

    private fun generateHash(seed: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        return bytes.take(4).joinToString("") { "%02x".format(it) } + "-$countryCode-FluxAO"
    }
}
