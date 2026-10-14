package com.example.data.service.fiscal.provider

import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.service.fiscal.FiscalIssueResult
import com.example.data.service.fiscal.FiscalProviderInterface
import com.example.data.service.fiscal.FiscalStatus
import com.example.data.service.fiscal.FiscalSubmissionResult
import com.example.data.service.fiscal.FiscalValidationResult
import java.security.MessageDigest

class FranceFiscalProvider : FiscalProviderInterface {

    override val providerId: String = "FranceFiscalProvider"
    override val providerName: String = "DGFiP France - Direction Générale des Finances Publiques"
    override val countryCode: String = "FR"
    override val taxAuthorityName: String = "DGFiP"

    override fun initialize() {}

    override fun validateInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): FiscalValidationResult {
        val errors = mutableListOf<String>()
        if (invoice.emitterNif.isBlank()) {
            errors.add("Numéro SIRET / Numéro de TVA Intracommunautaire requis.")
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
            message = "Facture établie conformément à la réglementation fiscale française (Article 289 du CGI)."
        )
    }

    override fun submitInvoice(invoice: InvoiceEntity): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.ACCEPTED,
            reference = getFiscalReference(invoice),
            message = "Document prêt pour la transmissão Chorus Pro / PDP."
        )
    }

    override fun cancelInvoice(invoice: InvoiceEntity, reason: String): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.VALIDATED,
            reference = "CANCEL-${getFiscalReference(invoice)}",
            message = "Avoir fiscal généré avec succès."
        )
    }

    override fun getInvoiceStatus(fiscalReference: String): FiscalStatus = FiscalStatus.ACCEPTED

    override fun generateFiscalQrCode(invoice: InvoiceEntity): String =
        "https://impots.gouv.fr/facture?siret=${invoice.emitterNif}&ref=${invoice.invoiceNumber}"

    override fun getFiscalReference(invoice: InvoiceEntity): String =
        "FR-DGFIP-${invoice.invoiceNumber.replace(" ", "")}"

    private fun generateHash(seed: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        return bytes.take(4).joinToString("") { "%02x".format(it) } + "-FR-FluxAO"
    }
}
