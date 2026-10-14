package com.example.data.service

import com.example.data.local.dao.UserEntitlementDao
import com.example.data.local.entity.UserEntitlementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EntitlementService(
    private val userEntitlementDao: UserEntitlementDao
) {

    fun getEntitlementsFlow(userId: String = "default_user"): Flow<List<UserEntitlementEntity>> {
        return userEntitlementDao.getAllEntitlementsFlow(userId)
    }

    fun getActiveEntitlementsFlow(userId: String = "default_user"): Flow<List<UserEntitlementEntity>> {
        return userEntitlementDao.getAllEntitlementsFlow(userId).map { list ->
            list.filter { it.isCurrentlyActive }
        }
    }

    suspend fun isFeatureActive(featureCode: String, userId: String = "default_user"): Boolean = withContext(Dispatchers.IO) {
        val entitlement = userEntitlementDao.getActiveEntitlementByCode(userId, featureCode)
        entitlement?.isCurrentlyActive ?: false
    }

    suspend fun hasFeature(featureCode: String, userId: String = "default_user"): Boolean {
        return isFeatureActive(featureCode, userId)
    }

    suspend fun getActiveEntitlement(featureCode: String, userId: String = "default_user"): UserEntitlementEntity? = withContext(Dispatchers.IO) {
        val entitlement = userEntitlementDao.getActiveEntitlementByCode(userId, featureCode)
        if (entitlement != null && entitlement.isCurrentlyActive) entitlement else null
    }

    suspend fun grantEntitlement(
        rewardCode: String,
        rewardTitle: String,
        rewardId: String,
        durationDays: Int? = 30,
        userId: String = "default_user"
    ): UserEntitlementEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val expiresAt = if (durationDays != null) now + (durationDays.toLong() * 24 * 60 * 60 * 1000) else null

        val entitlement = UserEntitlementEntity(
            id = "${rewardCode}_${userId}_$now",
            userId = userId,
            rewardId = rewardId,
            rewardCode = rewardCode,
            rewardTitle = rewardTitle,
            status = "active",
            activatedAtEpoch = now,
            expiresAtEpoch = expiresAt,
            createdAtEpoch = now
        )

        userEntitlementDao.insertOrUpdateEntitlement(entitlement)
        entitlement
    }
}
