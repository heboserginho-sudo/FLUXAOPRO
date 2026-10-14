package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoryById(id: Long): CategoryEntity? =
        categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    suspend fun deleteCategoryById(id: Long) =
        categoryDao.deleteCategoryById(id)

    suspend fun countTransactionsWithCategory(categoryName: String): Int =
        categoryDao.countTransactionsWithCategory(categoryName)

    suspend fun migrateCategoryAndRemove(oldCategory: CategoryEntity, newCategoryName: String) {
        categoryDao.migrateTransactionsCategory(oldCategory.name, newCategoryName)
        categoryDao.deleteCategory(oldCategory)
    }
}
