package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: String): CompanyEntity?

    @Query("SELECT * FROM companies WHERE slug = :slug LIMIT 1")
    suspend fun getCompanyBySlug(slug: String): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)

    @Update
    suspend fun updateCompany(company: CompanyEntity)

    @Query("UPDATE companies SET status = :status WHERE id = :companyId")
    suspend fun updateCompanyStatus(companyId: String, status: String)

    @Query("DELETE FROM companies WHERE id = :id")
    suspend fun deleteCompanyById(id: String)
}
