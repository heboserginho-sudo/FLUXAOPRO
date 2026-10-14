package com.example.data.service

import android.util.Log
import com.example.data.local.dao.UserEntitlementDao
import com.example.data.local.dao.UserFeatureUnlockDao
import com.example.data.local.entity.UserEntitlementEntity
import com.example.data.local.entity.UserFeatureUnlockEntity
import com.example.data.model.FeatureKeyHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized service managing feature unlocks and persistent permissions.
 * Guarantees that once unlocked, features stay unlocked across app restarts,
 * screen transitions, and navigation changes.
 *
 * Implements:
 * 1. Fast in-memory cache for instant zero-latency UI checks.
 * 2. Room database persistence (user_feature_unlocks & user_entitlements).
 * 3. Supabase cloud sync (user_feature_unlocks table).
 */
class FeatureUnlockService(
    private val userFeatureUnlockDao: UserFeatureUnlockDao,
    private val userEntitlementDao: UserEntitlementDao?,
    private val pointsService: PointsService,
    private val externalScope: CoroutineScope
) {

    // Fast in-memory set of unlocked keys and aliases
    private val unlockedKeysCache = ConcurrentHashMap.newKeySet<String>()

    private val _unlockedFeaturesState = MutableStateFlow<Set<String>>(emptySet())
    val unlockedFeaturesState: StateFlow<Set<String>> = _unlockedFeaturesState.asStateFlow()

    init {
        // Preload all unlocks synchronously / immediately on launch
        externalScope.launch(Dispatchers.IO) {
            refreshUnlocksFromDatabase("default_user")
            listenToDatabaseChanges("default_user")
        }
    }

    private suspend fun refreshUnlocksFromDatabase(userId: String) {
        val newSet = mutableSetOf<String>()

        // 1. Load from user_feature_unlocks
        try {
            val list = userFeatureUnlockDao.getAllUnlocksOnce(userId)
            for (item in list) {
                if (item.isCurrentlyActive) {
                    val aliases = FeatureKeyHelper.getAllAliases(item.featureKey)
                    newSet.addAll(aliases)
                }
            }
        } catch (e: Exception) {
            Log.e("FeatureUnlockService", "Error reading user_feature_unlocks: ${e.message}")
        }

        // 2. Load from user_entitlements (backward compatibility)
        if (userEntitlementDao != null) {
            try {
                val entitlements = userEntitlementDao.getAllEntitlementsOnce(userId)
                for (ent in entitlements) {
                    if (ent.isCurrentlyActive) {
                        val aliases = FeatureKeyHelper.getAllAliases(ent.rewardCode)
                        newSet.addAll(aliases)
                    }
                }
            } catch (e: Exception) {
                Log.e("FeatureUnlockService", "Error reading user_entitlements: ${e.message}")
            }
        }

        unlockedKeysCache.clear()
        unlockedKeysCache.addAll(newSet)
        _unlockedFeaturesState.value = newSet.toSet()
    }

    private fun listenToDatabaseChanges(userId: String) {
        externalScope.launch(Dispatchers.IO) {
            userFeatureUnlockDao.getAllUnlocksFlow(userId).collect { list ->
                val activeKeys = mutableSetOf<String>()
                for (item in list) {
                    if (item.isCurrentlyActive) {
                        activeKeys.addAll(FeatureKeyHelper.getAllAliases(item.featureKey))
                    }
                }
                // Also retain entitlements
                if (userEntitlementDao != null) {
                    try {
                        val ents = userEntitlementDao.getAllEntitlementsOnce(userId)
                        for (e in ents) {
                            if (e.isCurrentlyActive) {
                                activeKeys.addAll(FeatureKeyHelper.getAllAliases(e.rewardCode))
                            }
                        }
                    } catch (_: Exception) {}
                }
                unlockedKeysCache.clear()
                unlockedKeysCache.addAll(activeKeys)
                _unlockedFeaturesState.value = activeKeys.toSet()
            }
        }
    }

    /**
     * Synchronous check for UI components and navigation.
     * Guaranteed instant and non-blocking.
     */
    fun isFeatureUnlocked(userId: String = "default_user", featureKey: String): Boolean {
        val canonical = FeatureKeyHelper.normalize(featureKey)
        if (canonical == FeatureKeyHelper.CASHFLOW) return true
        val aliases = FeatureKeyHelper.getAllAliases(featureKey)
        if (aliases.any { unlockedKeysCache.contains(it) }) {
            return true
        }
        return false
    }

    suspend fun checkFeatureUnlock(userId: String = "default_user", featureKey: String): Boolean = withContext(Dispatchers.IO) {
        val canonical = FeatureKeyHelper.normalize(featureKey)
        if (canonical == FeatureKeyHelper.CASHFLOW) return@withContext true
        val aliases = FeatureKeyHelper.getAllAliases(featureKey)
        if (aliases.any { unlockedKeysCache.contains(it) }) {
            return@withContext true
        }
        try {
            val entity = userFeatureUnlockDao.getActiveUnlock(userId, aliases)
            if (entity != null && entity.isCurrentlyActive) {
                unlockedKeysCache.addAll(aliases)
                return@withContext true
            }
        } catch (_: Exception) {}
        return@withContext false
    }

    /**
     * Unlocks a feature by consuming user points, persisting to Room,
     * updating memory cache, and synchronizing to Supabase.
     */
    suspend fun unlockFeature(
        userId: String = "default_user",
        featureKey: String,
        pointsCost: Int,
        featureTitle: String,
        durationDays: Int? = 30
    ): AwardPointsResult = withContext(Dispatchers.IO) {
        val canonical = FeatureKeyHelper.normalize(featureKey)

        // 1. Verify if already unlocked
        if (checkFeatureUnlock(userId, canonical)) {
            return@withContext AwardPointsResult.AlreadyAwarded("A funcionalidade '$featureTitle' já está desbloqueada na sua conta!")
        }

        // 2. Spend points through PointsService
        val rewardId = "rew_$canonical"
        val spendResult = pointsService.spendPoints(
            userId = userId,
            pointsToSpend = pointsCost,
            rewardId = rewardId,
            rewardTitle = featureTitle
        )

        if (spendResult !is AwardPointsResult.Success) {
            return@withContext spendResult
        }

        // 3. Persist to Room: user_feature_unlocks
        val now = System.currentTimeMillis()
        val expiresAt = if (durationDays != null) now + (durationDays.toLong() * 24 * 60 * 60 * 1000) else null

        val unlockEntity = UserFeatureUnlockEntity(
            id = "unlock_${userId}_${canonical}",
            userId = userId,
            featureKey = canonical,
            unlocked = true,
            unlockedAtEpoch = now,
            pointsSpent = pointsCost,
            expiresAtEpoch = expiresAt,
            createdAtEpoch = now,
            updatedAtEpoch = now
        )
        userFeatureUnlockDao.insertOrUpdate(unlockEntity)

        // 4. Persist to Room: user_entitlements (for existing entitlement consumers)
        if (userEntitlementDao != null) {
            val entitlement = UserEntitlementEntity(
                id = "${canonical}_${userId}_$now",
                userId = userId,
                rewardId = rewardId,
                rewardCode = canonical,
                rewardTitle = featureTitle,
                status = "active",
                activatedAtEpoch = now,
                expiresAtEpoch = expiresAt,
                createdAtEpoch = now
            )
            userEntitlementDao.insertOrUpdateEntitlement(entitlement)
        }

        // 5. Update in-memory cache immediately
        val allAliases = FeatureKeyHelper.getAllAliases(canonical)
        unlockedKeysCache.addAll(allAliases)
        _unlockedFeaturesState.value = unlockedKeysCache.toSet()

        // 6. Asynchronously sync to Supabase
        externalScope.launch(Dispatchers.IO) {
            syncUnlockToSupabase(
                userId = userId,
                featureKey = canonical,
                pointsSpent = pointsCost,
                unlockedAtEpoch = now
            )
        }

        AwardPointsResult.Success(
            pointsAwarded = pointsCost,
            newBalance = spendResult.newBalance,
            transactionId = spendResult.transactionId
        )
    }

    /**
     * Best-effort REST sync with Supabase user_feature_unlocks table.
     */
    private fun syncUnlockToSupabase(
        userId: String,
        featureKey: String,
        pointsSpent: Int,
        unlockedAtEpoch: Long
    ) {
        try {
            val supabaseUrl = "https://placeholder-supabase.supabase.co"
            val supabaseKey = "placeholder-anon-key"

            val url = URL("$supabaseUrl/rest/v1/user_feature_unlocks")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("apikey", supabaseKey)
            conn.setRequestProperty("Authorization", "Bearer $supabaseKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates")
            conn.doOutput = true
            conn.connectTimeout = 3000
            conn.readTimeout = 3000

            val json = JSONObject().apply {
                put("id", "unlock_${userId}_${featureKey}")
                put("user_id", userId)
                put("feature_key", featureKey)
                put("unlocked", true)
                put("points_spent", pointsSpent)
            }

            conn.outputStream.use { os ->
                os.write(json.toString().toByteArray())
            }

            val responseCode = conn.responseCode
            Log.d("FeatureUnlockService", "Supabase sync response code: $responseCode")
            conn.disconnect()
        } catch (e: Exception) {
            // Offline or placeholder - local SQLite/Room guarantees persistence
            Log.d("FeatureUnlockService", "Local unlock stored offline. Supabase sync skipped: ${e.message}")
        }
    }
}
