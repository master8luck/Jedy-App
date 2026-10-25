package com.kit.jedyapp.sdk.admob

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kit.jedyapp.sdk.adDebug
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.api.NativeAdHandle

/** Demo units intentionally used for the assignment, in every build variant. */
class AdMobProvider(context: Context) : AdsProvider {
    private val context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private var initialized = false
    private var initializing = false
    private val pending = mutableListOf<() -> Unit>()
    private var interstitial: InterstitialAd? = null
    private var loadingInterstitial = false
    private var loadedAt = 0L

    override fun initialize() {
        if (initialized || initializing) return
        initializing = true
        MobileAds.initialize(context) {
            main.post {
                initialized = true
                initializing = false
                pending.toList().also { pending.clear() }.forEach { it() }
            }
        }
    }

    private fun whenReady(action: () -> Unit) {
        if (initialized) action() else {
            pending += action
            initialize()
        }
    }

    override fun loadNative(onLoaded: (NativeAdHandle?) -> Unit) = whenReady {
        AdLoader.Builder(context, NATIVE_UNIT)
            .forNativeAd { ad -> onLoaded(AdMobNativeHandle(ad)) }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) { onLoaded(null) }
            })
            .build().loadAd(AdRequest.Builder().build())
    }

    override fun preloadInterstitial() = whenReady {
        if (interstitial != null && isInterstitialExpired(loadedAt, android.os.SystemClock.elapsedRealtime())) {
            interstitial = null
        }
        if (!loadingInterstitial && interstitial == null) {
            loadingInterstitial = true
            InterstitialAd.load(context, INTERSTITIAL_UNIT, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        loadingInterstitial = false
                        interstitial = ad
                        loadedAt = android.os.SystemClock.elapsedRealtime()
                        adDebug("Interstitial loaded")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loadingInterstitial = false
                        adDebug("Interstitial load failed: code=${error.code}, ${error.message}")
                    }
                })
        }
    }

    override fun showInterstitial(activity: Activity): Boolean {
        val ad = interstitial ?: run {
            adDebug("Interstitial skipped: not loaded")
            return false
        }
        interstitial = null
        if (activity.isFinishing || activity.isDestroyed ||
            isInterstitialExpired(loadedAt, android.os.SystemClock.elapsedRealtime())) {
            adDebug("Interstitial skipped: invalid host or expired ad")
            return false
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { ad.fullScreenContentCallback = null }
            override fun onAdFailedToShowFullScreenContent(error: AdError) { ad.fullScreenContentCallback = null }
        }
        adDebug("Showing interstitial")
        ad.show(activity)
        return true
    }

    private companion object {
        const val NATIVE_UNIT = "ca-app-pub-3940256099942544/2247696110"
        const val INTERSTITIAL_UNIT = "ca-app-pub-3940256099942544/1033173712"
    }
}

private const val INTERSTITIAL_MAX_AGE_MS = 3_600_000L

internal fun isInterstitialExpired(loadedAt: Long, now: Long): Boolean =
    now - loadedAt >= INTERSTITIAL_MAX_AGE_MS
