package com.example.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ads.AdScreenContext
import com.example.data.ads.AdsConfiguration
import com.example.util.AdsManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Reusable, responsive Google AdMob Banner Composable.
 * Complies with Google AdMob design and labeling policies.
 * Never displays on disallowed or critical screens.
 */
@Composable
fun AdMobBannerView(
    screenContext: AdScreenContext,
    config: AdsConfiguration,
    modifier: Modifier = Modifier
) {
    if (!AdsManager.isAdAllowedOnScreen(screenContext, config)) {
        return
    }

    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    val adUnitId = remember(config) { AdsManager.getBannerAdUnitId(config) }

    val adView = remember(adUnitId) {
        AdView(context).apply {
            setAdUnitId(adUnitId)
            val displayMetrics = context.resources.displayMetrics
            val density = displayMetrics.density
            val adWidth = (displayMetrics.widthPixels / density).toInt()
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAdLoaded = true
                    Log.d("AdMobBannerView", "Banner ad loaded successfully ($adUnitId)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    isAdLoaded = false
                    Log.w("AdMobBannerView", "Banner ad failed to load: ${error.message} (code: ${error.code})")
                }
            }
            loadAd(AdsManager.buildAdRequest())
        }
    }

    DisposableEffect(adView) {
        onDispose {
            try {
                adView.destroy()
            } catch (e: Exception) {
                Log.e("AdMobBannerView", "Error destroying AdView: ${e.message}")
            }
        }
    }

    AnimatedVisibility(visible = isAdLoaded) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("admob_banner_container"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Policy-required discreet ad disclosure badge
                Text(
                    text = "Publicidade",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    factory = { adView }
                )
            }
        }
    }
}
