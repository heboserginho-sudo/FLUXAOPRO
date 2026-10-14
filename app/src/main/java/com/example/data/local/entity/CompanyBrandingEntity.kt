package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "company_branding")
data class CompanyBrandingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val companyId: String,
    val primaryColor: String = "#0F172A",
    val secondaryColor: String = "#D4AF37",
    val accentColor: String = "#059669",
    val backgroundColor: String = "#F8FAFC",
    val textColor: String = "#0F172A",
    val logoUrl: String = "",
    val faviconUrl: String = "",
    val appName: String = "FluxAO Business",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val updatedAtEpoch: Long = System.currentTimeMillis()
)
