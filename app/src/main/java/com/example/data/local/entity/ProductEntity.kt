package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Produtos e Serviços do catálogo da empresa
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = "PRODUCT", // "PRODUCT", "SERVICE"
    val skuOrCode: String = "",
    val unit: String = "un", // "un", "m", "m2", "kg", "hora"
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockAlert: Double = 5.0,
    val category: String = "Geral",
    val notes: String = "",
    val companyId: String = "default_company",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
