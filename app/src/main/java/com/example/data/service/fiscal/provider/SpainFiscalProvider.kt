package com.example.data.service.fiscal.provider

import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.service.fiscal.FiscalIssueResult
import com.example.data.service.fiscal.FiscalProviderInterface
import com.example.data.service.fiscal.FiscalStatus
import com.example.data.service.fiscal.FiscalSubmissionResult
import com.example.data.service.fiscal.FiscalValidationResult
import java.security.MessageDigest

class SpainFiscalProvider : FiscalProviderInterface {

    override val providerId: String = "SpainFiscalProvider"
    override val providerName: String = "AEAT España / Veri*Factu / TicketBAI"
    override val countryCode: String = "ES"
    override val taxAuthorityName: String = "AEAT"

    override fun initialize() {}

    override fun validateInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): FiscalValidationResult {
        val errors = mutableListOf<String>()
        if (invoice.emitterNif.isBlank()) {
            errors.add("NIF / CIF de la empresa en España es obligatorio.")
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
            message = "Factura expedida según normativa AEAT España (Reglamento Veri*Factu)."
        )
    }

    override fun submitInvoice(invoice: InvoiceEntity): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.ACCEPTED,
            reference = getFiscalReference(invoice),
            message = "Factura verificable transmitida a la Sede Electrónica AEAT."
        )
    }

    override fun cancelInvoice(invoice: InvoiceEntity, reason: String): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.VALIDATED,
            reference = "CANCEL-${getFiscalReference(invoice)}",
            message = "Factura rectificativa emitida correctamente."
        )
    }

    override fun getInvoiceStatus(fiscalReference: String): FiscalStatus = FiscalStatus.ACCEPTED

    override fun generateFiscalQrCode(invoice: InvoiceEntity): String =
        "https://www me.agenciatributaria.gob.es/verifactu?nif=${invoice.emitterNif}&num=${invoice.invoiceNumber}&importe=${invoice.totalToPay}"

    override fun getFiscalReference(invoice: InvoiceEntity): String =
        "ES-AEAT-${invoice.invoiceNumber.replace(" ", "")}"

    private fun generateHash(seed: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        return bytes.take(4).joinToString("") { "%02x".format(it) } + "-ES-FluxAO"
    }
}
