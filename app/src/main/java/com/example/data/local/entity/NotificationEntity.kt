package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Central de notificações inteligentes do negócio
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "DUE_SOON", "OVERDUE", "CLIENT_LATE", "BUDGET_APPROVED", "BUDGET_EXPIRED", "LOW_BALANCE", "PAYMENT_RECEIVED", "LOW_STOCK"
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedEntityId: Long? = null,
    val actionRoute: String = "",
    val companyId: String = "default_company"
) {
    val formattedTime: String
        get() = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestampMillis))
}
