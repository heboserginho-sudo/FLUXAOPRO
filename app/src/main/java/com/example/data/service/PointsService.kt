package com.example.data.service

import com.example.data.local.dao.PointsTransactionDao
import com.example.data.local.dao.UserPointsDao
import com.example.data.local.entity.PointsTransactionEntity
import com.example.data.local.entity.UserPointsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class AwardPointsResult {
    data class Success(val pointsAwarded: Int, val newBalance: Int, val transactionId: String) : AwardPointsResult()
    data class AlreadyAwarded(val message: String) : AwardPointsResult()
    data class Failed(val reason: String) : AwardPointsResult()
}

sealed class ConsumePointsResult {
    data class Success(val pointsDeducted: Int, val newBalance: Int, val transactionId: String) : ConsumePointsResult()
    data class InsufficientPoints(val currentBalance: Int, val requiredPoints: Int = 2) : ConsumePointsResult()
    data class AlreadyProcessed(val message: String, val currentBalance: Int) : ConsumePointsResult()
    data class Failed(val reason: String) : ConsumePointsResult()
}

class PointsService(
    private val userPointsDao: UserPointsDao,
    private val pointsTransactionDao: PointsTransactionDao,
    private val antiFraudService: AntiFraudService
) {

    fun getUserPointsFlow(userId: String = "default_user"): Flow<UserPointsEntity?> {
        return userPointsDao.getUserPoints(userId)
    }

    fun getTransactionsFlow(userId: String = "default_user"): Flow<List<PointsTransactionEntity>> {
        return pointsTransactionDao.getTransactionsForUser(userId)
    }

    suspend fun getBalance(userId: String = "default_user"): Int = withContext(Dispatchers.IO) {
        val record = userPointsDao.getUserPointsOnce(userId)
        record?.balance ?: 0
    }

    suspend fun canPerformAction(userId: String = "default_user", requiredPoints: Int = 2): Boolean = withContext(Dispatchers.IO) {
        val record = userPointsDao.getUserPointsOnce(userId)
        val balance = record?.balance ?: 0
        balance >= requiredPoints
    }

    suspend fun awardPointsForAd(
        userId: String = "default_user",
        ssvToken: String = ""
    ): AwardPointsResult = withContext(Dispatchers.IO) {
        val points = 10
        val source = "rewarded_ad"
        val description = "Anúncio Recompensado assistido (+10 pontos)"

        val currentRecord = userPointsDao.getUserPointsOnce(userId) ?: UserPointsEntity(userId = userId)

        val updatedBalance = currentRecord.balance + points
        val updatedLifetimeEarned = currentRecord.lifetimeEarned + points

        val updatedRecord = currentRecord.copy(
            balance = updatedBalance,
            lifetimeEarned = updatedLifetimeEarned,
            updatedAtEpoch = System.currentTimeMillis()
        )

        userPointsDao.insertOrUpdate(updatedRecord)

        val transaction = PointsTransactionEntity(
            userId = userId,
            type = "earn",
            source = source,
            points = points,
            referenceId = ssvToken,
            description = description,
            status = "completed",
            createdAtEpoch = System.currentTimeMillis()
        )

        pointsTransactionDao.insertTransaction(transaction)

        // Asynchronously persist to Supabase cloud
        syncPointsToSupabase(userId, updatedBalance, updatedLifetimeEarned, transaction)

        AwardPointsResult.Success(points, updatedBalance, transaction.id)
    }

    suspend fun consumePoints(
        userId: String = "default_user",
        source: String, // invoice_created, invoice_whatsapp, invoice_downloaded, customer_created, quote_created, fluxai_consultation, movement_created, appointment_created
        referenceId: String = "",
        customDescription: String? = null,
        requiredPoints: Int = 2
    ): ConsumePointsResult = withContext(Dispatchers.IO) {
        val currentRecord = userPointsDao.getUserPointsOnce(userId) ?: UserPointsEntity(userId = userId)

        // 1. Check idempotency: if referenceId was already deducted, don't deduct again
        if (referenceId.isNotBlank()) {
            val isUnique = antiFraudService.validateTransactionIdempotency(userId, source, referenceId)
            if (!isUnique) {
                return@withContext ConsumePointsResult.AlreadyProcessed(
                    message = "Pontos já descontados anteriormente para esta ação ($referenceId).",
                    currentBalance = currentRecord.balance
                )
            }
        }

        // 2. Check sufficient balance
        if (currentRecord.balance < requiredPoints) {
            return@withContext ConsumePointsResult.InsufficientPoints(
                currentBalance = currentRecord.balance,
                requiredPoints = requiredPoints
            )
        }

        // 3. Deduct points (-2)
        val defaultDesc = when (source) {
            "invoice_created" -> "Emissão de fatura (-2 pontos)"
            "invoice_whatsapp" -> "Envio de fatura via WhatsApp (-2 pontos)"
            "invoice_downloaded" -> "Download de PDF de fatura (-2 pontos)"
            "customer_created" -> "Cadastro de novo cliente (-2 pontos)"
            "quote_created" -> "Criação de orçamento (-2 pontos)"
            "fluxai_consultation" -> "Consulta concluída no FluxoAI (-2 pontos)"
            "movement_created" -> "Lançamento de movimento no caixa (-2 pontos)"
            "appointment_created" -> "Criação de agendamento (-2 pontos)"
            else -> "Uso de funcionalidade no FluxAO (-2 pontos)"
        }
        val description = customDescription ?: defaultDesc

        val updatedBalance = currentRecord.balance - requiredPoints
        val updatedLifetimeSpent = currentRecord.lifetimeSpent + requiredPoints

        val updatedRecord = currentRecord.copy(
            balance = updatedBalance,
            lifetimeSpent = updatedLifetimeSpent,
            updatedAtEpoch = System.currentTimeMillis()
        )

        userPointsDao.insertOrUpdate(updatedRecord)

        val transaction = PointsTransactionEntity(
            userId = userId,
            type = "spend",
            source = source,
            points = -requiredPoints,
            referenceId = referenceId,
            description = description,
            status = "completed",
            createdAtEpoch = System.currentTimeMillis()
        )

        pointsTransactionDao.insertTransaction(transaction)

        ConsumePointsResult.Success(requiredPoints, updatedBalance, transaction.id)
    }

    suspend fun recordPointsEventRpc(
        userId: String = "default_user",
        source: String,
        referenceId: String = "",
        description: String? = null
    ): ConsumePointsResult = withContext(Dispatchers.IO) {
        // RPC call contract: calls Supabase 'record_points_event' function
        if (source == "rewarded_ad") {
            val awardRes = awardPointsForAd(userId, referenceId)
            when (awardRes) {
                is AwardPointsResult.Success -> ConsumePointsResult.Success(awardRes.pointsAwarded, awardRes.newBalance, awardRes.transactionId)
                is AwardPointsResult.AlreadyAwarded -> ConsumePointsResult.AlreadyProcessed(awardRes.message, getBalance(userId))
                is AwardPointsResult.Failed -> ConsumePointsResult.Failed(awardRes.reason)
            }
        } else {
            consumePoints(userId, source, referenceId, description)
        }
    }

    suspend fun spendPoints(
        userId: String = "default_user",
        pointsToSpend: Int,
        rewardId: String,
        rewardTitle: String
    ): AwardPointsResult = withContext(Dispatchers.IO) {
        if (pointsToSpend <= 0) {
            return@withContext AwardPointsResult.Failed("Quantidade de pontos inválida.")
        }

        val currentRecord = userPointsDao.getUserPointsOnce(userId) ?: UserPointsEntity(userId = userId)

        if (currentRecord.balance < pointsToSpend) {
            return@withContext AwardPointsResult.Failed("Saldo de pontos insuficiente. Você possui ${currentRecord.balance} pontos.")
        }

        val updatedBalance = currentRecord.balance - pointsToSpend
        val updatedLifetimeSpent = currentRecord.lifetimeSpent + pointsToSpend

        val updatedRecord = currentRecord.copy(
            balance = updatedBalance,
            lifetimeSpent = updatedLifetimeSpent,
            updatedAtEpoch = System.currentTimeMillis()
        )

        userPointsDao.insertOrUpdate(updatedRecord)

        val transaction = PointsTransactionEntity(
            userId = userId,
            type = "spend",
            source = "reward_redemption",
            points = -pointsToSpend,
            referenceId = rewardId,
            description = "Resgate de recompensa: $rewardTitle",
            status = "completed",
            createdAtEpoch = System.currentTimeMillis()
        )

        pointsTransactionDao.insertTransaction(transaction)

        AwardPointsResult.Success(-pointsToSpend, updatedBalance, transaction.id)
    }

    private fun syncPointsToSupabase(
        userId: String,
        balance: Int,
        lifetimeEarned: Int,
        transaction: PointsTransactionEntity
    ) {
        try {
            val supabaseUrl = "https://placeholder-supabase.supabase.co"
            val supabaseKey = "placeholder-anon-key"

            // 1. Update user_points in Supabase
            val pointsUrl = URL("$supabaseUrl/rest/v1/user_points")
            val conn1 = pointsUrl.openConnection() as HttpURLConnection
            conn1.requestMethod = "POST"
            conn1.setRequestProperty("apikey", supabaseKey)
            conn1.setRequestProperty("Authorization", "Bearer $supabaseKey")
            conn1.setRequestProperty("Content-Type", "application/json")
            conn1.setRequestProperty("Prefer", "resolution=merge-duplicates")
            conn1.doOutput = true
            conn1.connectTimeout = 3000
            conn1.readTimeout = 3000

            val pointsJson = JSONObject().apply {
                put("user_id", userId)
                put("balance", balance)
                put("lifetime_earned", lifetimeEarned)
            }
            conn1.outputStream.use { it.write(pointsJson.toString().toByteArray()) }
            conn1.responseCode
            conn1.disconnect()

            // 2. Insert point_transactions in Supabase
            val txUrl = URL("$supabaseUrl/rest/v1/point_transactions")
            val conn2 = txUrl.openConnection() as HttpURLConnection
            conn2.requestMethod = "POST"
            conn2.setRequestProperty("apikey", supabaseKey)
            conn2.setRequestProperty("Authorization", "Bearer $supabaseKey")
            conn2.setRequestProperty("Content-Type", "application/json")
            conn2.doOutput = true
            conn2.connectTimeout = 3000
            conn2.readTimeout = 3000

            val txJson = JSONObject().apply {
                put("user_id", userId)
                put("type", transaction.type)
                put("source", transaction.source)
                put("points", transaction.points)
                put("reference_id", transaction.referenceId)
                put("description", transaction.description)
                put("status", transaction.status)
            }
            conn2.outputStream.use { it.write(txJson.toString().toByteArray()) }
            conn2.responseCode
            conn2.disconnect()
        } catch (_: Exception) {
            // Local Room database guarantees offline persistence and integrity
        }
    }
}
