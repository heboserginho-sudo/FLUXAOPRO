package com.example.data.service.fiscal

import com.example.data.local.dao.FiscalAuditLogDao
import com.example.data.local.entity.FiscalAuditLogEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow

class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val fiscalProviderService: FiscalProviderService = FiscalProviderService(),
    private val taxService: TaxService = TaxService(),
    private val currencyService: CurrencyService = CurrencyService(),
    private val documentNumberService: DocumentNumberService = DocumentNumberService(),
    private val invoiceTemplateService: InvoiceTemplateService = InvoiceTemplateService(),
    private val fiscalAuditLogDao: FiscalAuditLogDao? = null
) {

    fun getFiscalProviderService(): FiscalProviderService = fiscalProviderService
    fun getTaxService(): TaxService = taxService
    fun getCurrencyService(): CurrencyService = currencyService
    fun getDocumentNumberService(): DocumentNumberService = documentNumberService
    fun getInvoiceTemplateService(): InvoiceTemplateService = invoiceTemplateService

    suspend fun createAndIssueInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>,
        userId: String = "user_default"
    ): Result<Long> {
        val countryConfig = CountryFiscalRegistry.getByCode(invoice.countryCode)
        val provider = fiscalProviderService.getProviderForCountry(invoice.countryCode)

        // 1. Validation phase
        val validation = provider.validateInvoice(invoice, items)
        if (!validation.isValid) {
            val errorMsg = validation.errors.joinToString("\n")
            logAudit(userId, 0, "VALIDATION_FAILED", provider.providerId, "REJECTED", errorMsg)
            return Result.failure(Exception("Falha na validação fiscal (${provider.taxAuthorityName}):\n$errorMsg"))
        }

        // 2. Issue phase
        val issueResult = provider.issueInvoice(invoice, items)
        if (!issueResult.success) {
            logAudit(userId, 0, "EMISSION_FAILED", provider.providerId, "ERROR", issueResult.message)
            return Result.failure(Exception("Erro na emissão do documento fiscal: ${issueResult.message}"))
        }

        // 3. Prepare final entity
        val updatedInvoice = invoice.copy(
            fiscalValidationHash = issueResult.fiscalValidationHash,
            fiscalReference = issueResult.fiscalReference,
            qrCodeData = issueResult.qrCodeData,
            fiscalProviderId = provider.providerId,
            fiscalStatus = if (countryConfig.requireFiscalValidation) "VALIDATED" else "NOT_REQUIRED"
        )

        // 4. Save to Repository
        val savedId = invoiceRepository.saveInvoice(updatedInvoice, items)

        // 5. Submit to Fiscal Authority asynchronously if required
        val submitResult = provider.submitInvoice(updatedInvoice)

        logAudit(
            userId = userId,
            invoiceId = savedId,
            action = "ISSUE_INVOICE",
            providerId = provider.providerId,
            status = submitResult.status.name,
            details = "Fatura ${updatedInvoice.invoiceNumber} emitida com sucesso [${provider.taxAuthorityName}]. ${submitResult.message}"
        )

        return Result.success(savedId)
    }

    suspend fun cancelInvoice(
        invoiceId: Long,
        reason: String,
        userId: String = "user_default"
    ): Result<Boolean> {
        val invoice = invoiceRepository.getInvoiceByIdOnce(invoiceId)
            ?: return Result.failure(Exception("Fatura não encontrada com ID $invoiceId"))

        val provider = fiscalProviderService.getProviderForCountry(invoice.countryCode)
        val cancelResult = provider.cancelInvoice(invoice, reason)

        if (cancelResult.success) {
            invoiceRepository.updateInvoiceStatus(invoiceId, com.example.data.model.invoice.InvoiceStatus.CANCELLED)
            logAudit(
                userId = userId,
                invoiceId = invoiceId,
                action = "CANCEL_INVOICE",
                providerId = provider.providerId,
                status = cancelResult.status.name,
                details = "Fatura ${invoice.invoiceNumber} anulada. Motivo: $reason"
            )
            return Result.success(true)
        } else {
            return Result.failure(Exception("Não foi possível anular a fatura: ${cancelResult.message}"))
        }
    }

    private suspend fun logAudit(
        userId: String,
        invoiceId: Long,
        action: String,
        providerId: String,
        status: String,
        details: String
    ) {
        fiscalAuditLogDao?.insertLog(
            FiscalAuditLogEntity(
                userId = userId,
                invoiceId = invoiceId,
                action = action,
                providerId = providerId,
                status = status,
                details = details
            )
        )
    }
}
