package com.example.data.repository

import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class SaleRepository(
    private val saleDao: SaleDao,
    private val transactionDao: TransactionDao,
    private val productDao: ProductDao,
    private val accountDao: AccountDao
) {
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()

    fun getSalesByClient(clientId: Long): Flow<List<SaleEntity>> = saleDao.getSalesByClient(clientId)

    suspend fun getSaleById(id: Long): SaleEntity? = saleDao.getSaleById(id)

    suspend fun getSaleItems(saleId: Long): List<SaleItemEntity> = saleDao.getItemsForSale(saleId)

    suspend fun registerSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        syncCashFlow: Boolean = true
    ): Long {
        val saleId = saleDao.saveSaleWithItems(sale, items)

        // Abate estoque dos produtos vendidos
        for (item in items) {
            if (item.productId != null && item.type == "PRODUCT") {
                productDao.decrementStock(item.productId, item.quantity)
            }
        }

        // Se houver valor recebido, registra a entrada no caixa / conta bancária
        if (syncCashFlow && sale.paidAmount > 0.0) {
            val tx = TransactionEntity(
                type = TransactionType.INCOME,
                amount = sale.paidAmount,
                description = "Venda ${sale.saleNumber} - ${sale.clientName}",
                category = "Venda",
                dateEpochMillis = sale.dateEpochMillis,
                paymentMethod = sale.paymentMethod,
                status = TransactionStatus.PAID,
                notes = sale.notes,
                accountId = sale.accountId,
                accountName = sale.accountName,
                contactName = sale.clientName,
                contactType = "CLIENT",
                relatedSaleId = saleId
            )
            transactionDao.insertTransaction(tx)

            if (sale.accountId != null) {
                accountDao.adjustBalance(sale.accountId, sale.paidAmount)
            }
        }

        return saleId
    }

    suspend fun deleteSale(sale: SaleEntity) = saleDao.deleteSale(sale)

    suspend fun deleteSaleById(id: Long) = saleDao.deleteSaleById(id)
}
