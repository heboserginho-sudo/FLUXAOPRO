package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fiscal_audit_logs")
data class FiscalAuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestampEpoch: Long = System.currentTimeMillis(),
    val userId: String = "system",
    val invoiceId: Long = 0,
    val action: String, // CREATE, EMISSION, SUBMISSION, VALIDATION, REJECTION, CANCELLATION, SYNC
    val providerId: String,
    val requestReference: String = "",
    val responseReference: String = "",
    val status: String = "SUCCESS",
    val details: String = ""
)
