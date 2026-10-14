package com.example.data.repository

import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)
}
