package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CompanySettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanySettingsDao {
    @Query("SELECT * FROM company_settings WHERE companyId = :companyId LIMIT 1")
    fun getSettingsByCompanyIdFlow(companyId: String): Flow<CompanySettingsEntity?>

    @Query("SELECT * FROM company_settings WHERE companyId = :companyId LIMIT 1")
    suspend fun getSettingsByCompanyId(companyId: String): CompanySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: CompanySettingsEntity)

    @Update
    suspend fun updateSettings(settings: CompanySettingsEntity)
}
