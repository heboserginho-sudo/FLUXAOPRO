package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CompanyBrandingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyBrandingDao {
    @Query("SELECT * FROM company_branding WHERE companyId = :companyId LIMIT 1")
    fun getBrandingByCompanyIdFlow(companyId: String): Flow<CompanyBrandingEntity?>

    @Query("SELECT * FROM company_branding WHERE companyId = :companyId LIMIT 1")
    suspend fun getBrandingByCompanyId(companyId: String): CompanyBrandingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranding(branding: CompanyBrandingEntity)

    @Update
    suspend fun updateBranding(branding: CompanyBrandingEntity)
}
