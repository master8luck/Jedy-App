package com.kit.jedyapp.sdk.api

import android.app.Activity
import android.content.Context
import android.view.View

/** Provider-specific objects never leave the SDK implementation. */
interface AdsProvider {
    fun initialize()
    fun loadNative(onLoaded: (NativeAdHandle?) -> Unit)
    fun preloadInterstitial()
    fun showInterstitial(activity: Activity): Boolean
}

interface NativeAdHandle : AutoCloseable {
    fun createView(context: Context, textColor: Int, surfaceColor: Int): View
    override fun close()
}
