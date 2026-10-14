package com.example.data.service

import com.example.data.local.dao.PointsTransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AntiFraudService(
    private val pointsTransactionDao: PointsTransactionDao
) {
    private var lastRewardedAdTimestamp: Long = 0L

    /**
     * Validação anti-fraude: sem limites artificiais de anúncios diários.
     * O usuário pode assistir a quantos anúncios o Google AdMob tiver disponível.
     */
    suspend fun canUserWatchRewardedAd(userId: String): AntiFraudCheckResult = withContext(Dispatchers.IO) {
        // Sem limite artificial de anúncios por dia:
        // A disponibilidade de anúncios é determinada exclusivamente pelo Google AdMob.
        AntiFraudCheckResult.Allowed
    }

    suspend fun validateTransactionIdempotency(userId: String, source: String, referenceId: String): Boolean = withContext(Dispatchers.IO) {
        if (referenceId.isBlank()) return@withContext true // Not checking idempotency if blank
        val existing = pointsTransactionDao.findExistingTransaction(userId, source, referenceId)
        existing == null
    }

    fun recordAdWatched() {
        lastRewardedAdTimestamp = System.currentTimeMillis()
    }
}

sealed class AntiFraudCheckResult {
    object Allowed : AntiFraudCheckResult()
    data class Denied(val reason: String) : AntiFraudCheckResult()
}

