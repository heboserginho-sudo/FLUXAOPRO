package com.example.data.repository

import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class AccountRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) {
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)

    suspend fun deleteAccountById(id: Long) = accountDao.deleteAccountById(id)

    /**
     * Realiza transferência interna entre contas.
     * Importante: A movimentação é registrada com tipo TRANSFER e NÃO afeta receitas ou despesas gerais da empresa.
     */
    suspend fun transferBetweenAccounts(
        sourceAccount: AccountEntity,
        destinationAccount: AccountEntity,
        amount: Double,
        description: String = "",
        notes: String = ""
    ) {
        if (amount <= 0.0) return

        // 1. Debita da conta de origem
        accountDao.adjustBalance(sourceAccount.id, -amount)
        // 2. Credita na conta de destino
        accountDao.adjustBalance(destinationAccount.id, amount)

        // 3. Registra a transação de transferência no histórico
        val desc = description.ifBlank { "Transferência de ${sourceAccount.name} para ${destinationAccount.name}" }
        val transferTx = TransactionEntity(
            type = TransactionType.TRANSFER,
            amount = amount,
            description = desc,
            category = "Transferência Interna",
            dateEpochMillis = System.currentTimeMillis(),
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            status = TransactionStatus.PAID,
            notes = notes,
            accountId = sourceAccount.id,
            accountName = sourceAccount.name,
            destinationAccountId = destinationAccount.id,
            destinationAccountName = destinationAccount.name
        )
        transactionDao.insertTransaction(transferTx)
    }
}
