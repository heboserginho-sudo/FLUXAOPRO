package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repositório responsável por mediar a comunicação entre o banco de dados Room (TransactionDao)
 * e a camada de visualização (ViewModel), assegurando que todas as operações de dados
 * sejam executadas de forma assíncrona fora da thread principal (via [Dispatchers.IO]).
 */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Construtor de conveniência a partir de [AppDatabase].
     */
    constructor(database: AppDatabase, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        transactionDao = database.transactionDao(),
        ioDispatcher = ioDispatcher
    )

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

    suspend fun insert(transaction: TransactionEntity): Long =
        withContext(ioDispatcher) {
            transactionDao.insertTransaction(transaction)
        }

    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long> =
        withContext(ioDispatcher) {
            transactionDao.insertTransactions(transactions)
        }

    suspend fun update(transaction: TransactionEntity) =
        withContext(ioDispatcher) {
            transactionDao.updateTransaction(transaction)
        }

    suspend fun delete(transaction: TransactionEntity) =
        withContext(ioDispatcher) {
            transactionDao.deleteTransaction(transaction)
        }

    suspend fun deleteById(id: Long) =
        withContext(ioDispatcher) {
            transactionDao.deleteTransactionById(id)
        }

    suspend fun deleteByIds(ids: List<Long>) =
        withContext(ioDispatcher) {
            transactionDao.deleteTransactionsByIds(ids)
        }

    suspend fun deleteAll() =
        withContext(ioDispatcher) {
            transactionDao.deleteAllTransactions()
        }

    suspend fun toggleStatus(id: Long, currentStatus: TransactionStatus) =
        withContext(ioDispatcher) {
            val newStatus = if (currentStatus == TransactionStatus.PAID) {
                TransactionStatus.PENDING
            } else {
                TransactionStatus.PAID
            }
            transactionDao.updateStatus(id, newStatus)
        }

    suspend fun updateStatus(id: Long, newStatus: TransactionStatus) =
        withContext(ioDispatcher) {
            transactionDao.updateStatus(id, newStatus)
        }
}
