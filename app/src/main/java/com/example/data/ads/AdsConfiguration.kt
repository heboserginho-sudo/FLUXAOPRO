package com.example.data.ads

import com.example.data.ads.mediation.AdSourceType

/**
 * Centralized AdMob & Mediation configuration model for FluxAO.
 * Holds real production IDs by default and individual format toggles.
 */
data class AdsConfiguration(
    val isAdsEnabled: Boolean = false,
    val appId: String = REAL_APP_ID,
    val bannerAdUnitId: String = REAL_BANNER_AD_UNIT_ID,
    val nativeAdUnitId: String = "",
    val interstitialAdUnitId: String = "",
    val rewardedAdUnitId: String = "",
    val appOpenAdUnitId: String = "",
    val isBannerEnabled: Boolean = false,
    val isNativeEnabled: Boolean = false,
    val isInterstitialEnabled: Boolean = false,
    val isRewardedEnabled: Boolean = false,
    val isAppOpenEnabled: Boolean = false,
    val useTestAdsInDebug: Boolean = false,
    val adSourceType: AdSourceType = AdSourceType.ADMOB_DIRECT
) {
    companion object {
        // Real Google AdMob IDs provided officially for FluxAO
        const val REAL_APP_ID = "ca-app-pub-1035644843815418~5405435498"
        const val REAL_BANNER_AD_UNIT_ID = "ca-app-pub-1035644843815418/6690597005"

        // Google Standard Test Ad Unit IDs (used exclusively during debug testing to protect account)
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }
}
