package com.example.data.repository

import com.example.data.local.dao.CategoryBudgetDao
import com.example.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

class CategoryBudgetRepository(private val budgetDao: CategoryBudgetDao) {

    val allCategoryBudgets: Flow<List<CategoryBudgetEntity>> = budgetDao.getAllCategoryBudgets()

    suspend fun getBudgetByCategory(categoryName: String): CategoryBudgetEntity? =
        budgetDao.getBudgetByCategory(categoryName)

    suspend fun setOrUpdateBudget(categoryName: String, limit: Double, month: Int = 0, year: Int = 2026) {
        val existing = budgetDao.getBudgetByCategory(categoryName)
        if (existing != null) {
            budgetDao.updateCategoryBudget(existing.copy(monthlyLimit = limit, month = month, year = year))
        } else {
            budgetDao.insertCategoryBudget(CategoryBudgetEntity(categoryName = categoryName, monthlyLimit = limit, month = month, year = year))
        }
    }

    suspend fun deleteBudget(id: Long) = budgetDao.deleteCategoryBudgetById(id)
}
