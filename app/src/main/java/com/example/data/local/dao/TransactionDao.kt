package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para gerenciamento de transações financeiras e fluxos de caixa no Room Database.
 * Inclui operações completas de inserção, deleção, atualização e consultas reativas via Flow.
 */
@Dao
interface TransactionDao {

    // ==========================================
    // MÉTODOS DE CONSULTA (QUERIES)
    // ==========================================

    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionByIdFlow(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE dateEpochMillis BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpochMillis DESC")
    fun getTransactionsBetween(startEpoch: Long, endEpoch: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateEpochMillis BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpochMillis DESC")
    suspend fun getTransactionsBetweenOnce(startEpoch: Long, endEpoch: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateEpochMillis DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY dateEpochMillis DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR destinationAccountId = :accountId ORDER BY dateEpochMillis DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE projectId = :projectId ORDER BY dateEpochMillis DESC")
    fun getTransactionsByProject(projectId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE contactName = :contactName ORDER BY dateEpochMillis DESC")
    fun getTransactionsByContact(contactName: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY dateEpochMillis ASC")
    fun getPendingTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY dateEpochMillis DESC")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%' ORDER BY dateEpochMillis DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND status = 'PAID'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND status = 'PAID'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND status = 'PAID' AND dateEpochMillis BETWEEN :startEpoch AND :endEpoch")
    fun getTotalIncomeBetween(startEpoch: Long, endEpoch: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND status = 'PAID' AND dateEpochMillis BETWEEN :startEpoch AND :endEpoch")
    fun getTotalExpenseBetween(startEpoch: Long, endEpoch: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionsCount(): Flow<Int>

    // ==========================================
    // MÉTODOS DE INSERÇÃO
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    // ==========================================
    // MÉTODOS DE ATUALIZAÇÃO
    // ==========================================

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransactions(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TransactionStatus)

    // ==========================================
    // MÉTODOS DE DELEÇÃO
    // ==========================================

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactionsByIds(ids: List<Long>)

    @Query("DELETE FROM transactions WHERE type = :type")
    suspend fun deleteTransactionsByType(type: TransactionType)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
