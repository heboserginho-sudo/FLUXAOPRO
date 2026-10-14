package com.example.data.ads.mediation

/**
 * Architecture definitions for future third-party AdMob Mediation and Partner Bidding.
 * Allows seamless integration of ad sources without modifying screen implementations.
 */
enum class AdSourceType(val displayName: String) {
    ADMOB_DIRECT("Google AdMob Direto"),
    WATERFALL_MEDIATION("Mediação Waterfall de Terceiros"),
    PARTNER_BIDDING("Partner Bidding / Open Bidding")
}

data class MediationNetworkConfig(
    val networkId: String,
    val networkName: String,
    val isEnabled: Boolean = false,
    val adapterClassName: String = "",
    val appId: String = "",
    val placementId: String = "",
    val eCpmFloor: Double = 0.0
)

interface AdPartnerBidder {
    val bidderId: String
    val bidderName: String
    fun initialize(appId: String, onComplete: (Boolean) -> Unit)
    fun requestBid(adUnitId: String, onBidReceived: (bidPrice: Double, bidToken: String) -> Unit)
}

interface AdMediationAdapter {
    val networkName: String
    fun configure(config: MediationNetworkConfig)
    fun isAvailable(): Boolean
}
