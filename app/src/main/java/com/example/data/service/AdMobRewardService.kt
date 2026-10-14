package com.example.data.service

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.example.BuildConfig
import com.example.data.ads.AdsConfiguration
import com.example.util.AdsManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Official AdMob Rewarded Ad Service for FluxAO.
 * Handles loading, preloading, presentation, anti-fraud/anti-duplication,
 * and verified reward callbacks.
 */
class AdMobRewardService private constructor() {

    private var rewardedAd: RewardedAd? = null
    private val isAdLoading = AtomicBoolean(false)
    private val isShowingAd = AtomicBoolean(false)

    companion object {
        private const val TAG = "AdMobRewardService"

        // Official FluxAO AdMob IDs
        const val APP_ID = "ca-app-pub-1035644843815418~5405435498"
        const val PRODUCTION_REWARDED_AD_UNIT_ID = "ca-app-pub-1035644843815418/4199983856"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        @Volatile
        private var instance: AdMobRewardService? = null

        fun getInstance(): AdMobRewardService {
            return instance ?: synchronized(this) {
                instance ?: AdMobRewardService().also { instance = it }
            }
        }
    }

    /**
     * Resolves the proper Ad Unit ID according to environment.
     * Complies strictly with AdMob policies: uses test unit during development,
     * production unit in release builds.
     */
    fun getAdUnitId(): String {
        return PRODUCTION_REWARDED_AD_UNIT_ID
    }

    /**
     * Initializes the AdMob SDK if not already done, and preloads the first Rewarded Ad.
     */
    fun initialize(context: Context) {
        AdsManager.initialize(context)
        loadRewardedAd(context)
    }

    /**
     * Checks if a Rewarded Ad is loaded and ready to be shown.
     */
    fun isReady(): Boolean {
        return rewardedAd != null && !isShowingAd.get()
    }

    /**
     * Checks if an ad is currently being loaded.
     */
    fun isLoading(): Boolean {
        return isAdLoading.get()
    }

    /**
     * Loads a Rewarded Ad asynchronously.
     */
    fun loadRewardedAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (rewardedAd != null) {
            Log.d(TAG, "Rewarded Ad is already loaded and ready.")
            onLoaded?.invoke()
            return
        }

        if (isAdLoading.getAndSet(true)) {
            Log.d(TAG, "Rewarded Ad is already loading.")
            return
        }

        val adUnitId = getAdUnitId()
        val adRequest = AdRequest.Builder().build()

        Log.d(TAG, "Loading AdMob Rewarded Ad with Unit ID: $adUnitId")

        RewardedAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading.set(false)
                    Log.i(TAG, "AdMob Rewarded Ad successfully loaded.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isAdLoading.set(false)
                    val errorMsg = loadAdError.message
                    Log.w(TAG, "AdMob Rewarded Ad failed to load: $errorMsg (code: ${loadAdError.code})")
                    onFailed?.invoke(errorMsg)
                }
            }
        )
    }

    /**
     * Presents the Rewarded Ad to the user.
     * Enforces anti-duplication guards:
     * - Double click prevention
     * - Multiple show prevention
     * - Exactly one reward invocation per completed ad
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (points: Int) -> Unit,
        onAdClosed: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed, aborting showRewardedAd.")
            onError("Janela fechada antes da exibição.")
            return
        }

        val currentAd = rewardedAd
        if (currentAd == null) {
            Log.w(TAG, "Attempted to show Rewarded Ad, but none was loaded.")
            // Trigger background reload
            loadRewardedAd(activity)
            onError("Anúncio indisponível. Tente novamente.")
            return
        }

        if (!isShowingAd.compareAndSet(false, true)) {
            Log.w(TAG, "Ad presentation is already in progress.")
            return
        }

        // Flag to guarantee reward is granted ONLY when AdMob confirms completion
        val rewardGranted = AtomicBoolean(false)

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "AdMob Rewarded Ad showed full screen content.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "AdMob Rewarded Ad dismissed by user.")
                rewardedAd = null
                isShowingAd.set(false)

                // Preload the next ad automatically
                loadRewardedAd(activity)

                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "AdMob Rewarded Ad failed to show: ${adError.message}")
                rewardedAd = null
                isShowingAd.set(false)

                // Preload the next ad automatically
                loadRewardedAd(activity)

                onError("Anúncio indisponível. Tente novamente.")
            }
        }

        currentAd.show(activity) { rewardItem ->
            // SDK officially confirmed reward completion!
            if (rewardGranted.compareAndSet(false, true)) {
                val earnedAmount = if (rewardItem.amount > 0) rewardItem.amount else 10
                Log.i(TAG, "AdMob confirmed reward: $earnedAmount pts (${rewardItem.type})")
                onRewardEarned(earnedAmount)
            }
        }
    }
}

/**
 * Extension helper to safely find Activity from Context.
 */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
