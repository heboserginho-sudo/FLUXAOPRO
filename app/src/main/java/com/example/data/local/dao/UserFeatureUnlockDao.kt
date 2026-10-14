package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserFeatureUnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFeatureUnlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(unlock: UserFeatureUnlockEntity)

    @Query("SELECT * FROM user_feature_unlocks WHERE userId = :userId AND featureKey IN (:keys) AND unlocked = 1 LIMIT 1")
    suspend fun getActiveUnlock(userId: String, keys: List<String>): UserFeatureUnlockEntity?

    @Query("SELECT * FROM user_feature_unlocks WHERE userId = :userId AND featureKey IN (:keys) AND unlocked = 1 LIMIT 1")
    fun getActiveUnlockSync(userId: String, keys: List<String>): UserFeatureUnlockEntity?

    @Query("SELECT * FROM user_feature_unlocks WHERE userId = :userId AND featureKey = :key LIMIT 1")
    suspend fun getUnlockByKey(userId: String, key: String): UserFeatureUnlockEntity?

    @Query("SELECT * FROM user_feature_unlocks WHERE userId = :userId AND unlocked = 1")
    fun getAllUnlocksFlow(userId: String = "default_user"): Flow<List<UserFeatureUnlockEntity>>

    @Query("SELECT * FROM user_feature_unlocks WHERE userId = :userId AND unlocked = 1")
    suspend fun getAllUnlocksOnce(userId: String = "default_user"): List<UserFeatureUnlockEntity>

    @Query("SELECT COUNT(*) FROM user_feature_unlocks WHERE userId = :userId AND featureKey IN (:keys) AND unlocked = 1")
    suspend fun countActiveUnlocks(userId: String, keys: List<String>): Int

    @Query("DELETE FROM user_feature_unlocks WHERE userId = :userId AND featureKey = :key")
    suspend fun deleteUnlock(userId: String, key: String)
}
