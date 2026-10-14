package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "user_entitlements")
data class UserEntitlementEntity(
    @PrimaryKey
    val id: String, // e.g. "reports_advanced_user123_1710000000"
    val userId: String = "default_user",
    val rewardId: String,
    val rewardCode: String, // reports_advanced, invoice_premium, subscription_discount, whatsapp_automation, vip_support, pro_plan
    val rewardTitle: String,
    val status: String = "active", // active, expired, cancelled
    val activatedAtEpoch: Long = System.currentTimeMillis(),
    val expiresAtEpoch: Long? = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // null for permanent
    val createdAtEpoch: Long = System.currentTimeMillis()
) {
    val isCurrentlyActive: Boolean
        get() {
            if (status != "active") return false
            val expires = expiresAtEpoch ?: return true
            return System.currentTimeMillis() < expires
        }

    val formattedExpirationDate: String
        get() {
            val expires = expiresAtEpoch ?: return "Acesso Permanente"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(expires))
        }
}
