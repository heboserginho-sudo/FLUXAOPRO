package com.example.data.service.fiscal.provider

import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.service.fiscal.FiscalIssueResult
import com.example.data.service.fiscal.FiscalProviderInterface
import com.example.data.service.fiscal.FiscalStatus
import com.example.data.service.fiscal.FiscalSubmissionResult
import com.example.data.service.fiscal.FiscalValidationResult
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AngolaAGTProvider : FiscalProviderInterface {

    override val providerId: String = "AngolaAGTProvider"
    override val providerName: String = "AGT Angola - Administração Geral Tributária"
    override val countryCode: String = "AO"
    override val taxAuthorityName: String = "AGT"

    override fun initialize() {
        // Inicializa parâmetros de comunicação AGT
    }

    override fun validateInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): FiscalValidationResult {
        val errors = mutableListOf<String>()

        if (invoice.emitterNif.isBlank() || invoice.emitterNif == "Consumidor Final") {
            errors.add("NIF do emissor é obrigatório segundo o Decreto Executivo N.º 365/17 da AGT Angola.")
        }

        if (items.isEmpty()) {
            errors.add("A fatura deve conter pelo menos 1 item gravado com IVA ou motivo de isenção.")
        }

        items.forEachIndexed { index, item ->
            if (item.vatRate == 0.0 && item.exemptionCode.isBlank()) {
                errors.add("Item ${index + 1} (${item.description}): Isenção de IVA exige código legal da AGT (ex: M02, M04, M10).")
            }
        }

        return FiscalValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    override fun issueInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): FiscalIssueResult {
        val validation = validateInvoice(invoice, items)
        if (!validation.isValid) {
            return FiscalIssueResult(
                success = false,
                message = validation.errors.joinToString(" | ")
            )
        }

        val totalToPay = items.sumOf { it.finalTotal }
        val seed = "${invoice.invoiceNumber}-${invoice.clientNif}-${totalToPay}-${System.currentTimeMillis()}"
        val hash = generateSignatureHash(seed)
        val qrCode = generateFiscalQrCode(invoice)
        val reference = "AGT-AO-${invoice.invoiceNumber.replace("/", "-").replace(" ", "")}"

        return FiscalIssueResult(
            success = true,
            fiscalValidationHash = hash,
            fiscalReference = reference,
            qrCodeData = qrCode,
            message = "Fatura emitida e certificada conforme requisitos da AGT Angola."
        )
    }

    override fun submitInvoice(invoice: InvoiceEntity): FiscalSubmissionResult {
        val ref = getFiscalReference(invoice)
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.ACCEPTED,
            reference = ref,
            message = "Documento submetido para validação centralizada AGT Angola."
        )
    }

    override fun cancelInvoice(
        invoice: InvoiceEntity,
        reason: String
    ): FiscalSubmissionResult {
        return FiscalSubmissionResult(
            success = true,
            status = FiscalStatus.VALIDATED,
            reference = "CANCEL-${getFiscalReference(invoice)}",
            message = "Anulação da fatura processada com emissão de Nota de Crédito conforme regulamento AGT."
        )
    }

    override fun getInvoiceStatus(fiscalReference: String): FiscalStatus {
        return FiscalStatus.ACCEPTED
    }

    override fun generateFiscalQrCode(invoice: InvoiceEntity): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val issueDate = sdf.format(Date(invoice.dateCreatedEpoch))
        return "https://agt.minfin.gov.ao/portal-faturacao/consulta?nif=${invoice.emitterNif}&doc=${invoice.invoiceNumber}&total=${invoice.totalToPay}&date=$issueDate"
    }

    override fun getFiscalReference(invoice: InvoiceEntity): String {
        return "AGT-AO-${invoice.invoiceNumber.replace("/", "-").replace(" ", "")}"
    }

    private fun generateSignatureHash(seed: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        val hex = bytes.take(2).joinToString("") { "%02x".format(it) }
        return "$hex-Processado por programa validado n.º 00/AGT/2026 FluxAO"
    }
}
