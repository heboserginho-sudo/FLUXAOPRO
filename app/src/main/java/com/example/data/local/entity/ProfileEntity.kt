package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String, // UUID de auth.users do Supabase
    val companyId: String = "",
    val fullName: String,
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val role: String = "company_admin", // super_admin, company_admin, employee, customer
    val status: String = "active",
    val language: String = "pt",
    val preferredLanguage: String = "pt",
    val country: String = "AO",
    val currency: String = "AOA",
    val timezone: String = "Africa/Luanda",
    val locale: String = "pt_AO",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
)
