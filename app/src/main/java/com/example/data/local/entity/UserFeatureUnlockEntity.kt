package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "user_feature_unlocks",
    indices = [
        Index(value = ["userId", "featureKey"], unique = true)
    ]
)
data class UserFeatureUnlockEntity(
    @PrimaryKey
    val id: String, // e.g. "unlock_${userId}_${featureKey}"
    val userId: String = "default_user",
    val featureKey: String, // Canonical key: agenda, invoices, clients, advanced_reports, fluxoai, whatsapp_automation
    val unlocked: Boolean = true,
    val unlockedAtEpoch: Long = System.currentTimeMillis(),
    val pointsSpent: Int = 0,
    val expiresAtEpoch: Long? = null, // null for permanent or 30 days
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
) {
    val isCurrentlyActive: Boolean
        get() {
            if (!unlocked) return false
            val expires = expiresAtEpoch ?: return true
            return System.currentTimeMillis() < expires
        }

    val formattedUnlockedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(unlockedAtEpoch))
        }
}
