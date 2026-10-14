package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.model.BudgetStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para gerenciamento de orçamentos e itens de orçamentos no Room Database.
 * Oferece métodos para criação, edição, remoção e consulta detalhada de orçamentos.
 */
@Dao
interface BudgetDao {

    // ==========================================
    // MÉTODOS DE CONSULTA DE ORÇAMENTOS
    // ==========================================

    @Query("SELECT * FROM budgets ORDER BY dateCreatedEpochMillis DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY dateCreatedEpochMillis DESC")
    suspend fun getAllBudgetsOnce(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetById(id: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetByIdOnce(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE status = :status ORDER BY dateCreatedEpochMillis DESC")
    fun getBudgetsByStatus(status: BudgetStatus): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE dateCreatedEpochMillis BETWEEN :startEpoch AND :endEpoch ORDER BY dateCreatedEpochMillis DESC")
    fun getBudgetsBetween(startEpoch: Long, endEpoch: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE clientName LIKE '%' || :query || '%' OR projectTitle LIKE '%' || :query || '%' ORDER BY dateCreatedEpochMillis DESC")
    fun searchBudgets(query: String): Flow<List<BudgetEntity>>

    @Query("SELECT COUNT(*) FROM budgets")
    fun getBudgetsCount(): Flow<Int>

    // ==========================================
    // MÉTODOS DE INSERÇÃO E ATUALIZAÇÃO
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>): List<Long>

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudgets(budgets: List<BudgetEntity>)

    @Query("UPDATE budgets SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: BudgetStatus)

    // ==========================================
    // MÉTODOS DE DELEÇÃO
    // ==========================================

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudgets(budgets: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("DELETE FROM budgets WHERE id IN (:ids)")
    suspend fun deleteBudgetsByIds(ids: List<Long>)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    // ==========================================
    // ITENS DO ORÇAMENTO (BUDGET ITEMS)
    // ==========================================

    @Query("SELECT * FROM budget_items WHERE budgetId = :budgetId ORDER BY id ASC")
    fun getItemsForBudget(budgetId: Long): Flow<List<BudgetItemEntity>>

    @Query("SELECT * FROM budget_items WHERE budgetId = :budgetId ORDER BY id ASC")
    suspend fun getItemsForBudgetSync(budgetId: Long): List<BudgetItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: BudgetItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<BudgetItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: BudgetItemEntity)

    @Delete
    suspend fun deleteItem(item: BudgetItemEntity)

    @Query("DELETE FROM budget_items WHERE budgetId = :budgetId")
    suspend fun deleteItemsForBudget(budgetId: Long)
}
