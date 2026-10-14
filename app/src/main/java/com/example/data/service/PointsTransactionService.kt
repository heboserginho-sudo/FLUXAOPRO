package com.example.data.service

import com.example.data.local.entity.PointsTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PointsSummary(
    val currentBalance: Int = 0,
    val lifetimeEarned: Int = 0,
    val lifetimeSpent: Int = 0,
    val totalTransactionsCount: Int = 0,
    val rankTitle: String = "Iniciante"
)

class PointsTransactionService(
    private val pointsService: PointsService
) {

    fun getTransactionsFlow(userId: String = "default_user"): Flow<List<PointsTransactionEntity>> {
        return pointsService.getTransactionsFlow(userId)
    }

    fun getSummaryFlow(userId: String = "default_user"): Flow<PointsSummary> {
        return pointsService.getUserPointsFlow(userId).map { userPoints ->
            val balance = userPoints?.balance ?: 0
            val earned = userPoints?.lifetimeEarned ?: 0
            val spent = userPoints?.lifetimeSpent ?: 0

            val rank = when {
                earned >= 500 -> "Embaixador FluxAO 💎"
                earned >= 250 -> "Empresário Ouro 🥇"
                earned >= 100 -> "Parceiro Prata 🥈"
                earned >= 30 -> "Empreendedor Bronze 🥉"
                else -> "Membro Iniciante 🌟"
            }

            PointsSummary(
                currentBalance = balance,
                lifetimeEarned = earned,
                lifetimeSpent = spent,
                rankTitle = rank
            )
        }
    }
}
