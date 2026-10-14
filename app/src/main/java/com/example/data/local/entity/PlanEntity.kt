package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey
    val id: String, // plan_free, plan_starter, plan_pro, plan_business
    val name: String, // FREE, STARTER, PRO, BUSINESS
    val description: String,
    val price: Double = 0.0,
    val currency: String = "AOA",
    val billingPeriod: String = "monthly", // monthly, yearly
    val maxUsers: Int = 5,
    val maxProducts: Int = 100,
    val maxCustomers: Int = 100,
    val maxOrders: Int = 100,
    val featuresJson: String = "[]",
    val status: String = "active", // active, inactive
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
)
