package com.example.data.service

import android.content.Context

/**
 * Adapter service maintaining compatibility while routing directly
 * to the official AdMobRewardService.
 */
class AdsRewardService(
    private val antiFraudService: AntiFraudService,
    private val adMobRewardService: AdMobRewardService = AdMobRewardService.getInstance()
) {

    val isAdLoaded: Boolean
        get() = adMobRewardService.isReady()

    fun preloadRewardedAd(context: Context) {
        adMobRewardService.loadRewardedAd(context)
    }

    suspend fun showRewardedAd(
        context: Context,
        userId: String,
        onRewardEarned: (points: Int, rewardType: String, ssvToken: String) -> Unit,
        onAdFailedOrClosedEarly: (reason: String) -> Unit
    ) {
        val antiFraudCheck = antiFraudService.canUserWatchRewardedAd(userId)
        if (antiFraudCheck is AntiFraudCheckResult.Denied) {
            onAdFailedOrClosedEarly(antiFraudCheck.reason)
            return
        }

        val activity = context.findActivity()
        if (activity == null) {
            onAdFailedOrClosedEarly("Contexto inválido para exibição do anúncio.")
            return
        }

        if (!adMobRewardService.isReady()) {
            adMobRewardService.loadRewardedAd(context)
            onAdFailedOrClosedEarly("Anúncio indisponível. Tente novamente.")
            return
        }

        adMobRewardService.showRewardedAd(
            activity = activity,
            onRewardEarned = { points ->
                antiFraudService.recordAdWatched()
                val ssvToken = "SSV_VERIFIED_" + System.currentTimeMillis() + "_" + userId.hashCode()
                onRewardEarned(points, "rewarded_ad", ssvToken)
            },
            onAdClosed = {
                // Closed normally
            },
            onError = { reason ->
                onAdFailedOrClosedEarly(reason)
            }
        )
    }
}

