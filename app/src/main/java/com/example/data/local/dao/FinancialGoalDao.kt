package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FinancialGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialGoalDao {

    @Query("SELECT * FROM financial_goals ORDER BY deadlineEpochMillis ASC")
    fun getAllGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE status = 'ACTIVE' ORDER BY deadlineEpochMillis ASC")
    fun getActiveGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): FinancialGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<FinancialGoalEntity>)

    @Update
    suspend fun updateGoal(goal: FinancialGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoalEntity)

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
}
