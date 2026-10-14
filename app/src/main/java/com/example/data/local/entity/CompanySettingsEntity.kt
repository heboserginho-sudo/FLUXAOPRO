package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "company_settings")
data class CompanySettingsEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val companyId: String,
    val currency: String = "AOA",
    val timezone: String = "Africa/Luanda",
    val language: String = "pt-AO",
    val defaultTax: Double = 14.0,
    val businessHours: String = "08:00 - 18:00",
    val countryCode: String = "AO",
    val taxAuthority: String = "AGT",
    val fiscalProviderId: String = "AngolaAGTProvider",
    val taxIdType: String = "NIF",
    val taxIdNumber: String = "",
    val invoiceSeries: String = "AO2026",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
)
