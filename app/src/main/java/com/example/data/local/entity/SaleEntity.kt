package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Registro de Venda comercial de produtos e serviços
 */
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleNumber: String, // Ex: "VD-2026/001"
    val clientId: Long? = null,
    val clientName: String,
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.MULTICAIXA_EXPRESS,
    val accountId: Long? = null,
    val accountName: String = "Caixa / Banco",
    val status: String = "PAID", // "PAID", "PARTIAL", "PENDING", "CANCELLED"
    val notes: String = "",
    val companyId: String = "default_company",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateEpochMillis))
}
