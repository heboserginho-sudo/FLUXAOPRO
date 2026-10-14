package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Orçamento mensal estabelecido por categoria de despesa (ex: Material, Transporte, etc.)
 */
@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val monthlyLimit: Double,
    val month: Int = 0, // 0 = todos os meses, ou 1..12
    val year: Int = 2026,
    val createdAtEpoch: Long = System.currentTimeMillis()
)
