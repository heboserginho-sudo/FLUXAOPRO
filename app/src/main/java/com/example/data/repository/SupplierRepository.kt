package com.example.data.repository

import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

class SupplierRepository(private val supplierDao: SupplierDao) {

    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    suspend fun getSupplierById(id: Long): SupplierEntity? = supplierDao.getSupplierById(id)

    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)

    suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.updateSupplier(supplier)

    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.deleteSupplier(supplier)

    suspend fun deleteSupplierById(id: Long) = supplierDao.deleteSupplierById(id)
}
