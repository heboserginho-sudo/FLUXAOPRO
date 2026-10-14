package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadlineEpochMillis: Long = System.currentTimeMillis() + (30L * 86400000L),
    val status: String = "ACTIVE", // "ACTIVE", "REACHED", "AT_RISK", "CANCELLED"
    val type: String = "PROFIT", // "PROFIT", "SALES", "EXPENSE_REDUCTION", "EMERGENCY_RESERVE", "CUSTOM"
    val createdAtEpoch: Long = System.currentTimeMillis()
) {
    val progressPercent: Double
        get() = if (targetAmount > 0.0) ((currentAmount / targetAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0

    val formattedDeadline: String
        get() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(deadlineEpochMillis))
}
