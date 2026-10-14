package com.example.data.repository

import com.example.data.local.dao.FinancialGoalDao
import com.example.data.local.entity.FinancialGoalEntity
import kotlinx.coroutines.flow.Flow

class FinancialGoalRepository(private val goalDao: FinancialGoalDao) {

    val allGoals: Flow<List<FinancialGoalEntity>> = goalDao.getAllGoals()
    val activeGoals: Flow<List<FinancialGoalEntity>> = goalDao.getActiveGoals()

    suspend fun getGoalById(id: Long): FinancialGoalEntity? =
        goalDao.getGoalById(id)

    suspend fun saveGoal(goal: FinancialGoalEntity): Long =
        goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: FinancialGoalEntity) =
        goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: FinancialGoalEntity) =
        goalDao.deleteGoal(goal)

    suspend fun deleteGoalById(id: Long) =
        goalDao.deleteGoalById(id)
}
