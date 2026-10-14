package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.BudgetItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.BudgetStatus
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repositório responsável por mediar a comunicação entre o banco de dados Room
 * (armazenamento de registros financeiros, fluxos de caixa e orçamentos) e a camada de visualização (ViewModel).
 *
 * Garante que todas as operações de leitura pontual e escrita sejam executadas de forma assíncrona
 * em background thread via [Dispatchers.IO], e que os fluxos observáveis ([Flow]) emitam atualizações reativas.
 */
class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Construtor secundário para instanciação direta a partir de [AppDatabase].
     */
    constructor(database: AppDatabase, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        transactionDao = database.transactionDao(),
        budgetDao = database.budgetDao(),
        ioDispatcher = ioDispatcher
    )

    // ==========================================
    // FLUXOS REATIVOS: REGISTROS FINANCEIROS & CAIXA
    // ==========================================

    val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions().flowOn(ioDispatcher)

    val pendingTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getPendingTransactions().flowOn(ioDispatcher)

    val totalIncome: Flow<Double?> =
        transactionDao.getTotalIncome().flowOn(ioDispatcher)

    val totalExpense: Flow<Double?> =
        transactionDao.getTotalExpense().flowOn(ioDispatcher)

    val transactionsCount: Flow<Int> =
        transactionDao.getTransactionsCount().flowOn(ioDispatcher)

    fun getTransactionsBetween(startEpoch: Long, endEpoch: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startEpoch, endEpoch).flowOn(ioDispatcher)

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByType(type).flowOn(ioDispatcher)

    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(category).flowOn(ioDispatcher)

    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByAccount(accountId).flowOn(ioDispatcher)

    fun getTransactionsByProject(projectId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByProject(projectId).flowOn(ioDispatcher)

    fun getTransactionsByContact(contactName: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByContact(contactName).flowOn(ioDispatcher)

    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByStatus(status).flowOn(ioDispatcher)

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(query).flowOn(ioDispatcher)

    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit).flowOn(ioDispatcher)

    fun getTotalIncomeBetween(startEpoch: Long, endEpoch: Long): Flow<Double?> =
        transactionDao.getTotalIncomeBetween(startEpoch, endEpoch).flowOn(ioDispatcher)

    fun getTotalExpenseBetween(startEpoch: Long, endEpoch: Long): Flow<Double?> =
        transactionDao.getTotalExpenseBetween(startEpoch, endEpoch).flowOn(ioDispatcher)

    fun getTransactionByIdFlow(id: Long): Flow<TransactionEntity?> =
        transactionDao.getTransactionByIdFlow(id).flowOn(ioDispatcher)

    // ==========================================
    // OPERAÇÕES ASSÍNCRONAS: TRANSAÇÕES / CAIXA
    // ==========================================

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        withContext(ioDispatcher) {
            transactionDao.insertTransaction(transaction)
        }

    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long> =
        withContext(ioDispatcher) {
            transactionDao.insertTransactions(transactions)
        }

    suspend fun updateTransaction(transaction: TransactionEntity) =
        withContext(ioDispatcher) {
            transactionDao.updateTransaction(transaction)
        }

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        withContext(ioDispatcher) {
            transactionDao.deleteTransaction(transaction)
        }

    suspend fun deleteTransactionById(id: Long) =
        withContext(ioDispatcher) {
            transactionDao.deleteTransactionById(id)
        }

    suspend fun deleteTransactionsByIds(ids: List<Long>) =
        withContext(ioDispatcher) {
            transactionDao.deleteTransactionsByIds(ids)
        }

    suspend fun deleteAllTransactions() =
        withContext(ioDispatcher) {
            transactionDao.deleteAllTransactions()
        }

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        withContext(ioDispatcher) {
            transactionDao.getTransactionById(id)
        }

    suspend fun getAllTransactionsOnce(): List<TransactionEntity> =
        withContext(ioDispatcher) {
            transactionDao.getAllTransactionsOnce()
        }

    suspend fun getTransactionsBetweenOnce(startEpoch: Long, endEpoch: Long): List<TransactionEntity> =
        withContext(ioDispatcher) {
            transactionDao.getTransactionsBetweenOnce(startEpoch, endEpoch)
        }

    suspend fun toggleTransactionStatus(id: Long, currentStatus: TransactionStatus) =
        withContext(ioDispatcher) {
            val newStatus = if (currentStatus == TransactionStatus.PAID) {
                TransactionStatus.PENDING
            } else {
                TransactionStatus.PAID
            }
            transactionDao.updateStatus(id, newStatus)
        }

    suspend fun updateTransactionStatus(id: Long, newStatus: TransactionStatus) =
        withContext(ioDispatcher) {
            transactionDao.updateStatus(id, newStatus)
        }

    // ==========================================
    // FLUXOS REATIVOS: ORÇAMENTOS (BUDGETS)
    // ==========================================

    val allBudgets: Flow<List<BudgetEntity>> =
        budgetDao.getAllBudgets().flowOn(ioDispatcher)

    val budgetsCount: Flow<Int> =
        budgetDao.getBudgetsCount().flowOn(ioDispatcher)

    fun getBudgetById(id: Long): Flow<BudgetEntity?> =
        budgetDao.getBudgetById(id).flowOn(ioDispatcher)

    fun getBudgetsByStatus(status: BudgetStatus): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsByStatus(status).flowOn(ioDispatcher)

    fun getBudgetsBetween(startEpoch: Long, endEpoch: Long): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsBetween(startEpoch, endEpoch).flowOn(ioDispatcher)

    fun searchBudgets(query: String): Flow<List<BudgetEntity>> =
        budgetDao.searchBudgets(query).flowOn(ioDispatcher)

    fun getItemsForBudget(budgetId: Long): Flow<List<BudgetItemEntity>> =
        budgetDao.getItemsForBudget(budgetId).flowOn(ioDispatcher)

    // ==========================================
    // OPERAÇÕES ASSÍNCRONAS: ORÇAMENTOS
    // ==========================================

    suspend fun getBudgetByIdOnce(id: Long): BudgetEntity? =
        withContext(ioDispatcher) {
            budgetDao.getBudgetByIdOnce(id)
        }

    suspend fun getAllBudgetsOnce(): List<BudgetEntity> =
        withContext(ioDispatcher) {
            budgetDao.getAllBudgetsOnce()
        }

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

    suspend fun updateBudgetStatus(id: Long, status: BudgetStatus) =
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

    suspend fun deleteAllBudgets() =
        withContext(ioDispatcher) {
            budgetDao.deleteAllBudgets()
        }
}

/**
 * Alias de conveniência para casos onde a camada consumidora faça referência
 * como FinancialRecordRepository.
 */
typealias FinancialRecordRepository = FinancialRepository
