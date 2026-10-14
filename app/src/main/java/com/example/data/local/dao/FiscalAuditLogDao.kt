package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FiscalAuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FiscalAuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FiscalAuditLogEntity): Long

    @Query("SELECT * FROM fiscal_audit_logs ORDER BY timestampEpoch DESC")
    fun getAllLogs(): Flow<List<FiscalAuditLogEntity>>

    @Query("SELECT * FROM fiscal_audit_logs WHERE invoiceId = :invoiceId ORDER BY timestampEpoch DESC")
    fun getLogsForInvoice(invoiceId: Long): Flow<List<FiscalAuditLogEntity>>
}
