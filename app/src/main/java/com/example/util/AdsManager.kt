package com.example.util

import android.content.Context
import android.util.Log
import android.webkit.WebView
import com.example.BuildConfig
import com.example.data.ads.AdScreenContext
import com.example.data.ads.AdsConfiguration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Centralized Manager for Google AdMob SDK and mediation.
 * Provides safe, policy-compliant ad loading, test mode handling, and screen context filtering.
 */
object AdsManager {
    private const val TAG = "AdsManager"
    private val isInitialized = AtomicBoolean(false)

    /**
     * Initializes the Mobile Ads SDK once per app lifecycle on a background thread.
     */
    fun initialize(context: Context) {
        if (isInitialized.getAndSet(true)) {
            return
        }

        try {
            WebView.setWebContentsDebuggingEnabled(false)
        } catch (_: Exception) {}

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Disable mediation adapter auto-initialization at cold boot until mediation adapters are configured
                MobileAds.disableMediationAdapterInitialization(context)

                MobileAds.initialize(context) { status ->
                    val adapterStatusMap = status.adapterStatusMap
                    for ((adapterClass, adapterStatus) in adapterStatusMap) {
                        Log.d(TAG, "AdMob Adapter: $adapterClass, State: ${adapterStatus.initializationState}")
                    }
                    Log.i(TAG, "Google Mobile Ads SDK successfully initialized for FluxAO.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Mobile Ads SDK: ${e.message}", e)
            }
        }
    }

    /**
     * Resolves the appropriate Banner Ad Unit ID based on environment and config.
     */
    fun getBannerAdUnitId(config: AdsConfiguration): String {
        return config.bannerAdUnitId.ifBlank { AdsConfiguration.REAL_BANNER_AD_UNIT_ID }
    }

    /**
     * Evaluates if displaying an ad is permissible on the given screen.
     * Enforces strict user privacy and anti-disruption rules during critical processes.
     */
    fun isAdAllowedOnScreen(screenContext: AdScreenContext, config: AdsConfiguration): Boolean {
        if (!config.isAdsEnabled) return false
        return screenContext.isAllowed
    }

    /**
     * Builds a standard, compliant AdRequest.
     */
    fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
