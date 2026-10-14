package com.example.data.service

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.BuildConfig
import com.example.data.ads.AdScreenContext
import com.example.util.AdsManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Official AdMob App Open Ad Service for FluxAO.
 * Manages loading, presentation, lifecycle observation (foreground/background transitions),
 * safety timeouts, 60-second frequency intervals, and critical screen exclusion rules.
 *
 * App Open Ads NEVER grant points or modify user balance, and NEVER block the user from
 * entering the application.
 */
class AppOpenAdService private constructor() : Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0L
    private var lastAdDismissedOrShownTime: Long = 0L

    private val isAdLoading = AtomicBoolean(false)
    private val isShowingAd = AtomicBoolean(false)
    private val isLifecycleObserverRegistered = AtomicBoolean(false)

    private var currentActivityRef: WeakReference<Activity>? = null
    private var numStartedActivities = 0
    private var isAppInForeground = false

    /**
     * Flag indicating whether the user is actively engaged in a critical task
     * (e.g. creating invoice, editing budget, filling form, making payment, watching rewarded ad).
     */
    @Volatile
    private var isCriticalFlowActive: Boolean = false

    companion object {
        private const val TAG = "AppOpenAdService"

        // Official FluxAO AdMob App ID
        const val APP_ID = "ca-app-pub-1035644843815418~5405435498"

        // Google official App Open Ad sample test unit ID
        const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

        // Production App Open Ad unit ID (distinct from Rewarded Ad ca-app-pub-1035644843815418/4199983856)
        const val PRODUCTION_APP_OPEN_AD_UNIT_ID = "ca-app-pub-1035644843815418/8892147321"

        // Minimum interval between displaying App Open Ads (60 seconds)
        const val MIN_APP_OPEN_AD_INTERVAL_MS = 60_000L

        // AdMob Policy: Ads older than 4 hours are considered expired and should not be shown
        const val MAX_AD_AGE_MS = 4 * 60 * 60 * 1000L

        // Default splash screen timeout to guarantee user is never blocked
        const val DEFAULT_SAFETY_TIMEOUT_MS = 3_500L

        @Volatile
        private var instance: AppOpenAdService? = null

        fun getInstance(): AppOpenAdService {
            return instance ?: synchronized(this) {
                instance ?: AppOpenAdService().also { instance = it }
            }
        }
    }

    /**
     * Resolves the App Open Ad Unit ID according to environment.
     * Uses test unit during development/debug, and production unit for release builds.
     */
    fun getAdUnitId(): String {
        return PRODUCTION_APP_OPEN_AD_UNIT_ID
    }

    /**
     * Initializes the Mobile Ads SDK, registers lifecycle callbacks to track foreground transitions,
     * and preloads the initial App Open Ad.
     */
    fun initialize(context: Context) {
        AdsManager.initialize(context)

        val app = context.applicationContext as? Application
        if (app != null && isLifecycleObserverRegistered.compareAndSet(false, true)) {
            app.registerActivityLifecycleCallbacks(this)
            Log.d(TAG, "Registered ActivityLifecycleCallbacks for AppOpenAdService.")
        }

        loadAppOpenAd(context)
    }

    /**
     * Sets whether a critical user flow is currently active.
     * When true, App Open Ads will NOT be shown under any circumstances.
     */
    fun setCriticalFlowActive(active: Boolean) {
        this.isCriticalFlowActive = active
        Log.d(TAG, "Critical flow active set to: $active")
    }

    /**
     * Checks if a critical flow or screen is currently active.
     */
    fun isCriticalFlowActive(): Boolean = isCriticalFlowActive

    /**
     * Sets critical screen context directly from AdScreenContext enum.
     */
    fun updateScreenContext(screenContext: AdScreenContext) {
        setCriticalFlowActive(!screenContext.isAllowed)
    }

    /**
     * Checks if the currently loaded ad is expired (older than 4 hours).
     */
    private fun isAdExpired(): Boolean {
        if (loadTime == 0L) return true
        val timeDifference = System.currentTimeMillis() - loadTime
        return timeDifference >= MAX_AD_AGE_MS
    }

    /**
     * Checks if an App Open Ad is currently loaded, valid, and unexpired.
     */
    fun isReady(): Boolean {
        return appOpenAd != null && !isAdExpired() && !isShowingAd.get()
    }

    /**
     * Checks if an ad is currently being loaded.
     */
    fun isLoading(): Boolean {
        return isAdLoading.get()
    }

    /**
     * Checks if an ad is currently being displayed on screen.
     */
    fun isShowing(): Boolean {
        return isShowingAd.get()
    }

    /**
     * Evaluates if displaying an App Open Ad is permitted at the current moment:
     * - Ad is loaded and unexpired
     * - No ad is currently showing
     * - Minimum 60s interval has elapsed since last ad
     * - No critical flow is active
     */
    fun canShowAdNow(): Boolean {
        if (isCriticalFlowActive) return false
        if (isShowingAd.get()) return false
        if (appOpenAd == null) return false

        // Check 4-hour expiration policy
        if (System.currentTimeMillis() - loadTime >= MAX_AD_AGE_MS) {
            appOpenAd = null
            return false
        }

        // Check 60-second minimum interval between App Open Ads
        val timeSinceLastAd = System.currentTimeMillis() - lastAdDismissedOrShownTime
        if (timeSinceLastAd < MIN_APP_OPEN_AD_INTERVAL_MS) {
            return false
        }

        return true
    }

    /**
     * Loads an App Open Ad asynchronously if one is not already available or loading.
     */
    fun loadAppOpenAd(
        context: Context,
        onLoaded: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        if (isReady()) {
            Log.d(TAG, "App Open Ad is already loaded and ready.")
            onLoaded?.invoke()
            return
        }

        if (isAdLoading.getAndSet(true)) {
            Log.d(TAG, "App Open Ad is already loading.")
            return
        }

        val adUnitId = getAdUnitId()
        val adRequest = AdRequest.Builder().build()

        Log.d(TAG, "Loading App Open Ad with Unit ID: $adUnitId")

        AppOpenAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = System.currentTimeMillis()
                    isAdLoading.set(false)
                    Log.i(TAG, "App Open Ad successfully loaded.")
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    loadTime = 0L
                    isAdLoading.set(false)
                    val errorMsg = loadAdError.message
                    Log.w(TAG, "App Open Ad failed to load: $errorMsg (code: ${loadAdError.code})")
                    onAdFailedToLoad(errorMsg)
                    onFailed?.invoke(errorMsg)
                }
            }
        )
    }

    /**
     * Internal callback when ad fails to load.
     */
    private fun onAdFailedToLoad(error: String) {
        Log.d(TAG, "onAdFailedToLoad: $error")
    }

    /**
     * Internal callback when ad is dismissed.
     */
    private fun onAdDismissed() {
        Log.i(TAG, "onAdDismissed: App Open Ad closed by user.")
        lastAdDismissedOrShownTime = System.currentTimeMillis()
        appOpenAd = null
        isShowingAd.set(false)
    }

    /**
     * Internal callback when ad fails to show.
     */
    private fun onAdFailedToShow(error: String) {
        Log.e(TAG, "onAdFailedToShow: $error")
        lastAdDismissedOrShownTime = System.currentTimeMillis()
        appOpenAd = null
        isShowingAd.set(false)
    }

    /**
     * Presents the App Open Ad to the user.
     * NEVER blocks the user flow; always executes onDismissed once ad finishes or fails.
     */
    fun showAppOpenAd(
        activity: Activity,
        onDismissed: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed, aborting showAppOpenAd.")
            onDismissed()
            return
        }

        if (!canShowAdNow()) {
            Log.d(TAG, "showAppOpenAd called, but canShowAdNow() returned false.")
            onDismissed()
            return
        }

        val ad = appOpenAd
        if (ad == null) {
            Log.w(TAG, "Attempted to show App Open Ad, but none was available.")
            loadAppOpenAd(activity)
            onDismissed()
            return
        }

        if (!isShowingAd.compareAndSet(false, true)) {
            Log.w(TAG, "An ad is already currently showing.")
            onDismissed()
            return
        }

        val hasCompleted = AtomicBoolean(false)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "App Open Ad shown full screen.")
            }

            override fun onAdDismissedFullScreenContent() {
                onAdDismissed()
                // Automatically reload next ad for future resume
                loadAppOpenAd(activity)
                if (hasCompleted.compareAndSet(false, true)) {
                    onDismissed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                onAdFailedToShow(adError.message)
                // Automatically reload next ad
                loadAppOpenAd(activity)
                if (hasCompleted.compareAndSet(false, true)) {
                    onDismissed()
                }
            }
        }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing App Open Ad: ${e.message}", e)
            onAdFailedToShow(e.message ?: "Unknown error")
            if (hasCompleted.compareAndSet(false, true)) {
                onDismissed()
            }
        }
    }

    /**
     * Shows App Open Ad if available, otherwise invokes onComplete immediately.
     */
    fun showAppOpenAdIfAvailable(
        activity: Activity,
        onComplete: () -> Unit = {}
    ) {
        if (canShowAdNow()) {
            showAppOpenAd(activity, onDismissed = onComplete)
        } else {
            onComplete()
        }
    }

    /**
     * Cold start / Splash helper with strict safety timeout.
     * Guarantees that the app entry will NEVER hang or be blocked.
     * If an ad is ready within [timeoutMs], it is displayed; otherwise the user proceeds immediately.
     */
    fun showAppOpenAdWithTimeout(
        activity: Activity,
        timeoutMs: Long = DEFAULT_SAFETY_TIMEOUT_MS,
        onComplete: () -> Unit
    ) {
        // If ad is already loaded and allowed, show immediately
        if (canShowAdNow()) {
            showAppOpenAd(activity, onDismissed = onComplete)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val hasCompleted = AtomicBoolean(false)

        val timeoutRunnable = Runnable {
            if (hasCompleted.compareAndSet(false, true)) {
                Log.d(TAG, "Safety timeout reached ($timeoutMs ms). Proceeding to app immediately.")
                onComplete()
            }
        }

        handler.postDelayed(timeoutRunnable, timeoutMs)

        // Try loading if not already in progress
        loadAppOpenAd(
            context = activity,
            onLoaded = {
                handler.removeCallbacks(timeoutRunnable)
                if (hasCompleted.compareAndSet(false, true)) {
                    showAppOpenAd(activity, onDismissed = onComplete)
                }
            },
            onFailed = {
                handler.removeCallbacks(timeoutRunnable)
                if (hasCompleted.compareAndSet(false, true)) {
                    onComplete()
                }
            }
        )
    }

    // =========================================================================
    // Application.ActivityLifecycleCallbacks (Background -> Foreground Management)
    // =========================================================================

    override fun onActivityStarted(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        numStartedActivities++

        // Transition from Background to Foreground
        if (numStartedActivities == 1 && !isAppInForeground) {
            isAppInForeground = true
            Log.d(TAG, "App entered FOREGROUND.")

            // Check if App Open Ad should be shown on resume
            if (canShowAdNow()) {
                Log.i(TAG, "Showing App Open Ad on background -> foreground transition.")
                showAppOpenAd(activity)
            } else {
                // Ensure ad is preloaded for next foreground transition
                if (!isReady() && !isLoading()) {
                    loadAppOpenAd(activity)
                }
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        numStartedActivities--
        if (numStartedActivities <= 0) {
            numStartedActivities = 0
            isAppInForeground = false
            Log.d(TAG, "App entered BACKGROUND.")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() == activity) {
            currentActivityRef = null
        }
    }
}
