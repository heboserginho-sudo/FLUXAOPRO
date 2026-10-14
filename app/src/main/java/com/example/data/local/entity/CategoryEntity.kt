package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String = "Category",
    val colorHex: Long = 0xFFF59E0B,
    val isDefault: Boolean = false,
    val companyId: String = "default_company",
    val createdAtEpoch: Long = System.currentTimeMillis()
)
