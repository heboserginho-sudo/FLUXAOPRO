package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestão e controle financeiro por projeto (ex: "Cozinha Cliente João")
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Ex: "Cozinha Cliente João"
    val clientId: Long? = null,
    val clientName: String = "",
    val budgetedRevenue: Double = 0.0, // Ex: 850.000 Kz
    val actualRevenue: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val status: String = "IN_PROGRESS", // "PLANNING", "IN_PROGRESS", "COMPLETED", "DELIVERED", "CANCELLED"
    val startDateMillis: Long = System.currentTimeMillis(),
    val deadlineMillis: Long = System.currentTimeMillis() + (30L * 86400000L),
    val description: String = "",
    val notes: String = "",
    val companyId: String = "default_company",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    val formattedStartDate: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(startDateMillis))

    val formattedDeadline: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(deadlineMillis))
}
