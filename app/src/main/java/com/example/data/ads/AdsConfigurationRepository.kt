package com.example.data.ads

import android.content.Context
import android.content.SharedPreferences
import com.example.data.ads.mediation.AdSourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdsConfigurationRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AdsConfiguration> = _config.asStateFlow()

    private fun loadConfig(): AdsConfiguration {
        return AdsConfiguration(
            isAdsEnabled = prefs.getBoolean(KEY_ADS_ENABLED, false),
            appId = prefs.getString(KEY_APP_ID, AdsConfiguration.REAL_APP_ID) ?: AdsConfiguration.REAL_APP_ID,
            bannerAdUnitId = prefs.getString(KEY_BANNER_ID, AdsConfiguration.REAL_BANNER_AD_UNIT_ID) ?: AdsConfiguration.REAL_BANNER_AD_UNIT_ID,
            nativeAdUnitId = prefs.getString(KEY_NATIVE_ID, "") ?: "",
            interstitialAdUnitId = prefs.getString(KEY_INTERSTITIAL_ID, "") ?: "",
            rewardedAdUnitId = prefs.getString(KEY_REWARDED_ID, "") ?: "",
            appOpenAdUnitId = prefs.getString(KEY_APP_OPEN_ID, "") ?: "",
            isBannerEnabled = prefs.getBoolean(KEY_BANNER_ENABLED, false),
            isNativeEnabled = prefs.getBoolean(KEY_NATIVE_ENABLED, false),
            isInterstitialEnabled = prefs.getBoolean(KEY_INTERSTITIAL_ENABLED, false),
            isRewardedEnabled = prefs.getBoolean(KEY_REWARDED_ENABLED, false),
            isAppOpenEnabled = prefs.getBoolean(KEY_APP_OPEN_ENABLED, false),
            useTestAdsInDebug = prefs.getBoolean(KEY_USE_TEST_ADS_DEBUG, false),
            adSourceType = AdSourceType.valueOf(prefs.getString(KEY_AD_SOURCE, AdSourceType.ADMOB_DIRECT.name) ?: AdSourceType.ADMOB_DIRECT.name)
        )
    }

    fun saveConfig(newConfig: AdsConfiguration) {
        prefs.edit()
            .putBoolean(KEY_ADS_ENABLED, newConfig.isAdsEnabled)
            .putString(KEY_APP_ID, newConfig.appId)
            .putString(KEY_BANNER_ID, newConfig.bannerAdUnitId)
            .putString(KEY_NATIVE_ID, newConfig.nativeAdUnitId)
            .putString(KEY_INTERSTITIAL_ID, newConfig.interstitialAdUnitId)
            .putString(KEY_REWARDED_ID, newConfig.rewardedAdUnitId)
            .putString(KEY_APP_OPEN_ID, newConfig.appOpenAdUnitId)
            .putBoolean(KEY_BANNER_ENABLED, newConfig.isBannerEnabled)
            .putBoolean(KEY_NATIVE_ENABLED, newConfig.isNativeEnabled)
            .putBoolean(KEY_INTERSTITIAL_ENABLED, newConfig.isInterstitialEnabled)
            .putBoolean(KEY_REWARDED_ENABLED, newConfig.isRewardedEnabled)
            .putBoolean(KEY_APP_OPEN_ENABLED, newConfig.isAppOpenEnabled)
            .putBoolean(KEY_USE_TEST_ADS_DEBUG, newConfig.useTestAdsInDebug)
            .putString(KEY_AD_SOURCE, newConfig.adSourceType.name)
            .apply()

        _config.value = newConfig
    }

    companion object {
        private const val PREFS_NAME = "fluxao_admob_config_prefs"
        private const val KEY_ADS_ENABLED = "ads_enabled"
        private const val KEY_APP_ID = "admob_app_id"
        private const val KEY_BANNER_ID = "admob_banner_id"
        private const val KEY_NATIVE_ID = "admob_native_id"
        private const val KEY_INTERSTITIAL_ID = "admob_interstitial_id"
        private const val KEY_REWARDED_ID = "admob_rewarded_id"
        private const val KEY_APP_OPEN_ID = "admob_app_open_id"
        private const val KEY_BANNER_ENABLED = "format_banner_enabled"
        private const val KEY_NATIVE_ENABLED = "format_native_enabled"
        private const val KEY_INTERSTITIAL_ENABLED = "format_interstitial_enabled"
        private const val KEY_REWARDED_ENABLED = "format_rewarded_enabled"
        private const val KEY_APP_OPEN_ENABLED = "format_app_open_enabled"
        private const val KEY_USE_TEST_ADS_DEBUG = "use_test_ads_debug"
        private const val KEY_AD_SOURCE = "ad_source_type"
    }
}
