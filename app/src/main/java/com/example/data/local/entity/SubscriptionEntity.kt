package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String, // UUID
    val companyId: String,
    val planId: String,
    val status: String = "active", // trial, active, past_due, cancelled, expired
    val startedAtEpoch: Long = System.currentTimeMillis(),
    val expiresAtEpoch: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val trialEndsAtEpoch: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000),
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
)
