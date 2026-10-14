package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PointsTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsTransactionDao {

    @Query("SELECT * FROM points_transactions WHERE userId = :userId ORDER BY createdAtEpoch DESC")
    fun getTransactionsForUser(userId: String): Flow<List<PointsTransactionEntity>>

    @Query("SELECT * FROM points_transactions WHERE userId = :userId AND source = :source AND referenceId = :referenceId LIMIT 1")
    suspend fun findExistingTransaction(userId: String, source: String, referenceId: String): PointsTransactionEntity?

    @Query("SELECT COUNT(*) FROM points_transactions WHERE userId = :userId AND source = :source AND createdAtEpoch >= :sinceEpoch")
    suspend fun countSourceSince(userId: String, source: String, sinceEpoch: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PointsTransactionEntity)
}
