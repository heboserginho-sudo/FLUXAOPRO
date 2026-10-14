package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "points_transactions")
data class PointsTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val type: String, // earn, spend, adjustment, reversal
    val source: String, // rewarded_ad, invoice_created, invoice_whatsapp, quote_created, customer_created, appointment_created, admin_adjustment, reward_redemption
    val points: Int,
    val referenceId: String = "", // Unique ID of the action (e.g. invoice ID) to guarantee idempotency
    val description: String = "",
    val status: String = "completed", // completed, pending, failed
    val createdAtEpoch: Long = System.currentTimeMillis()
)
