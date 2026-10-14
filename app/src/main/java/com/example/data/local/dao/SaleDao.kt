package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY dateEpochMillis DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE clientId = :clientId ORDER BY dateEpochMillis DESC")
    fun getSalesByClient(clientId: Long): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSaleFlow(saleId: Long): Flow<List<SaleItemEntity>>

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteItemsForSale(saleId: Long)

    @Transaction
    suspend fun saveSaleWithItems(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = if (sale.id == 0L) {
            insertSale(sale)
        } else {
            updateSale(sale)
            deleteItemsForSale(sale.id)
            sale.id
        }
        val itemsWithId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithId)
        return saleId
    }
}
