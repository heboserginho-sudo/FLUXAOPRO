package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserEntitlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEntitlementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEntitlement(entitlement: UserEntitlementEntity)

    @Query("SELECT * FROM user_entitlements WHERE userId = :userId ORDER BY activatedAtEpoch DESC")
    fun getAllEntitlementsFlow(userId: String = "default_user"): Flow<List<UserEntitlementEntity>>

    @Query("SELECT * FROM user_entitlements WHERE userId = :userId AND rewardCode = :rewardCode AND status = 'active' ORDER BY activatedAtEpoch DESC LIMIT 1")
    suspend fun getActiveEntitlementByCode(userId: String = "default_user", rewardCode: String): UserEntitlementEntity?

    @Query("SELECT * FROM user_entitlements WHERE userId = :userId ORDER BY activatedAtEpoch DESC")
    suspend fun getAllEntitlementsOnce(userId: String = "default_user"): List<UserEntitlementEntity>
}
