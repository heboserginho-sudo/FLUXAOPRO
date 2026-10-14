package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val companyId: String = "",
    val action: String, // login, logout, create, update, delete, suspend, activate, payment, subscription_change
    val entityType: String = "",
    val entityId: String = "",
    val metadata: String = "",
    val ipAddress: String = "127.0.0.1",
    val createdAtEpoch: Long = System.currentTimeMillis()
)
