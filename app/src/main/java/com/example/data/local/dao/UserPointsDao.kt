package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserPointsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPointsDao {

    @Query("SELECT * FROM user_points WHERE userId = :userId LIMIT 1")
    fun getUserPoints(userId: String): Flow<UserPointsEntity?>

    @Query("SELECT * FROM user_points WHERE userId = :userId LIMIT 1")
    suspend fun getUserPointsOnce(userId: String): UserPointsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userPoints: UserPointsEntity)
}
