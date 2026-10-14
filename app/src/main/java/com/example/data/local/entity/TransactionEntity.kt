package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Movimentação financeira (Entrada, Saída ou Transferência entre contas)
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index("dateEpochMillis"),
        Index("type"),
        Index("status"),
        Index("category"),
        Index("accountId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val category: String,
    val dateEpochMillis: Long,
    val paymentMethod: PaymentMethod = PaymentMethod.MULTICAIXA_EXPRESS,
    val status: TransactionStatus = TransactionStatus.PAID,
    val notes: String = "",
    val relatedBudgetId: Long? = null,
    // Novos campos do sistema completo de movimentações
    val accountId: Long? = null,
    val accountName: String = "Dinheiro / Caixa",
    val destinationAccountId: Long? = null,
    val destinationAccountName: String = "",
    val contactName: String = "", // Cliente ou fornecedor
    val contactType: String = "", // "CLIENT", "SUPPLIER", "OTHER"
    val attachmentUri: String = "", // URI ou path do anexo/recibo
    val projectId: Long? = null,
    val relatedSaleId: Long? = null,
    val companyId: String = "default_company"
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateEpochMillis))
}
