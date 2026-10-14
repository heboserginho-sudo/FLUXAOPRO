package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceId"])]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val description: String,
    val unit: String = "Un",
    val quantity: Double = 1.0,
    val unitPriceKz: Double = 0.0,
    val discountPercent: Double = 0.0,
    val vatRate: Double = 14.0,
    val exemptionCode: String = ""
) {
    val grossTotal: Double get() = quantity * unitPriceKz
    val discountAmount: Double get() = grossTotal * (discountPercent / 100.0)
    val netTotal: Double get() = grossTotal - discountAmount
    val vatAmount: Double get() = netTotal * (vatRate / 100.0)
    val finalTotal: Double get() = netTotal + vatAmount
}
