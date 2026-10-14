package com.example.data.repository

import com.example.data.local.dao.BudgetDao
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.model.BudgetStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BudgetRepository(
    private val budgetDao: BudgetDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allBudgets: Flow<List<BudgetEntity>> =
        budgetDao.getAllBudgets().flowOn(ioDispatcher)

    val budgetsCount: Flow<Int> =
        budgetDao.getBudgetsCount().flowOn(ioDispatcher)

    fun getBudgetById(id: Long): Flow<BudgetEntity?> =
        budgetDao.getBudgetById(id).flowOn(ioDispatcher)

    suspend fun getBudgetByIdOnce(id: Long): BudgetEntity? =
        withContext(ioDispatcher) {
            budgetDao.getBudgetByIdOnce(id)
        }

    suspend fun getAllBudgetsOnce(): List<BudgetEntity> =
        withContext(ioDispatcher) {
            budgetDao.getAllBudgetsOnce()
        }

    fun getBudgetsByStatus(status: BudgetStatus): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsByStatus(status).flowOn(ioDispatcher)

    fun getBudgetsBetween(startEpoch: Long, endEpoch: Long): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsBetween(startEpoch, endEpoch).flowOn(ioDispatcher)

    fun searchBudgets(query: String): Flow<List<BudgetEntity>> =
        budgetDao.searchBudgets(query).flowOn(ioDispatcher)

    fun getItemsForBudget(budgetId: Long): Flow<List<BudgetItemEntity>> =
        budgetDao.getItemsForBudget(budgetId).flowOn(ioDispatcher)

    suspend fun getItemsForBudgetSync(budgetId: Long): List<BudgetItemEntity> =
        withContext(ioDispatcher) {
            budgetDao.getItemsForBudgetSync(budgetId)
        }

    suspend fun saveBudget(budget: BudgetEntity, items: List<BudgetItemEntity>): Long =
        withContext(ioDispatcher) {
            val budgetId = if (budget.id == 0L) {
                budgetDao.insertBudget(budget)
            } else {
                budgetDao.updateBudget(budget)
                budget.id
            }

            budgetDao.deleteItemsForBudget(budgetId)
            val itemsWithId = items.map { it.copy(budgetId = budgetId) }
            budgetDao.insertItems(itemsWithId)

            budgetId
        }

    suspend fun updateStatus(id: Long, status: BudgetStatus) =
        withContext(ioDispatcher) {
            budgetDao.updateStatus(id, status)
        }

    suspend fun deleteBudget(id: Long) =
        withContext(ioDispatcher) {
            budgetDao.deleteBudgetById(id)
        }

    suspend fun deleteBudgetsByIds(ids: List<Long>) =
        withContext(ioDispatcher) {
            budgetDao.deleteBudgetsByIds(ids)
        }

    suspend fun deleteAll() =
        withContext(ioDispatcher) {
            budgetDao.deleteAllBudgets()
        }
}
