package com.example.data.repository

import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.InvoiceItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.invoice.InvoiceItemData
import com.example.data.model.invoice.InvoiceStatus
import com.example.data.model.invoice.InvoiceType
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.Calendar
import java.util.Locale

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val transactionDao: TransactionDao? = null
) {

    fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    fun getInvoiceById(id: Long): Flow<InvoiceEntity?> = invoiceDao.getInvoiceById(id)

    suspend fun getInvoiceByIdOnce(id: Long): InvoiceEntity? = invoiceDao.getInvoiceByIdOnce(id)

    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItemEntity>> =
        invoiceDao.getItemsForInvoice(invoiceId)

    suspend fun getItemsForInvoiceSync(invoiceId: Long): List<InvoiceItemEntity> =
        invoiceDao.getItemsForInvoiceSync(invoiceId)

    suspend fun generateNextInvoiceNumber(type: InvoiceType): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val count = invoiceDao.countByType(type.name) + 1
        return String.format(Locale.ROOT, "%s %d/%03d", type.code, currentYear, count)
    }

    private fun generateFiscalHash(seed: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        val hex = bytes.take(2).joinToString("") { "%02x".format(it) }
        return "$hex-Processado por programa validado n.º 00/AGT/2026 FluxAO"
    }

    suspend fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemData>
    ): Long {
        // Calculate totals
        var totalGross = 0.0
        var totalDiscount = 0.0
        var totalNet = 0.0
        var totalVat = 0.0

        items.forEach { item ->
            totalGross += item.grossTotal
            totalDiscount += item.discountAmount
            totalNet += item.netTotal
            totalVat += item.vatAmount
        }

        val totalToPay = totalNet + totalVat
        val hash = if (invoice.fiscalValidationHash.isBlank()) {
            generateFiscalHash("${invoice.invoiceNumber}-${invoice.clientNif}-${totalToPay}-${System.currentTimeMillis()}")
        } else {
            invoice.fiscalValidationHash
        }

        val toSave = invoice.copy(
            totalGross = totalGross,
            totalDiscount = totalDiscount,
            totalNet = totalNet,
            totalVat = totalVat,
            totalToPay = totalToPay,
            fiscalValidationHash = hash
        )

        val invoiceId = if (toSave.id == 0L) {
            invoiceDao.insertInvoice(toSave)
        } else {
            invoiceDao.updateInvoice(toSave)
            invoiceDao.deleteItemsForInvoice(toSave.id)
            toSave.id
        }

        val itemEntities = items.map { item ->
            InvoiceItemEntity(
                invoiceId = invoiceId,
                description = item.description,
                unit = item.unit,
                quantity = item.quantity,
                unitPriceKz = item.unitPriceKz,
                discountPercent = item.discountPercent,
                vatRate = item.vatRate,
                exemptionCode = item.exemptionCode
            )
        }
        invoiceDao.insertItems(itemEntities)

        // If it's a Fatura-Recibo (FR) created with status PAID, optionally record directly into cash flow
        if (toSave.invoiceType == InvoiceType.FR && toSave.status == InvoiceStatus.PAID && transactionDao != null) {
            registerPaymentInCashFlow(toSave)
        }

        return invoiceId
    }

    suspend fun updateInvoiceStatus(id: Long, newStatus: InvoiceStatus, syncCashFlow: Boolean = true) {
        invoiceDao.updateStatus(id, newStatus)
        if (newStatus == InvoiceStatus.PAID && syncCashFlow && transactionDao != null) {
            val invoice = invoiceDao.getInvoiceByIdOnce(id)
            if (invoice != null) {
                registerPaymentInCashFlow(invoice)
            }
        }
    }

    private suspend fun registerPaymentInCashFlow(invoice: InvoiceEntity) {
        transactionDao?.insertTransaction(
            TransactionEntity(
                type = TransactionType.INCOME,
                amount = invoice.totalToPay,
                description = "Recebimento ${invoice.invoiceNumber} - ${invoice.clientName}",
                category = "Venda de Produtos",
                dateEpochMillis = System.currentTimeMillis(),
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                status = TransactionStatus.PAID
            )
        )
    }

    suspend fun deleteInvoice(id: Long) {
        invoiceDao.deleteInvoiceById(id)
    }
}
