package com.kit.jedyapp.sdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.policy.ReturnState
import com.kit.jedyapp.sdk.policy.onForeground

/** App-process lifetime owner. Does not retain host Activities. */
class AdsRuntime(
    private val application: Application,
    private val provider: AdsProvider,
    private val hostClass: Class<out Activity>,
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    private val preferences = application.getSharedPreferences("ad_returns", Application.MODE_PRIVATE)
    private var eligibleThisReturn = false
    private var started = false

    fun start() {
        if (started) return
        started = true
        provider.initialize()
        provider.preloadInterstitial()
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        val decision = onForeground(ReturnState(
            preferences.getBoolean("launched", false), preferences.getInt("returns", 0)
        ))
        preferences.edit().putBoolean("launched", decision.state.launched)
            .putInt("returns", decision.state.returns).apply()
        eligibleThisReturn = decision.eligible
        adDebug("Foreground: returns=${decision.state.returns}, eligible=${decision.eligible}")
        if (decision.state.returns < 2) provider.preloadInterstitial()
    }

    override fun onStop(owner: LifecycleOwner) { eligibleThisReturn = false }

    override fun onActivityResumed(activity: Activity) {
        if (!hostClass.isInstance(activity)) return
        if (eligibleThisReturn) {
            eligibleThisReturn = false
            // Skip when not loaded; never interrupt later in the user's session.
            val shown = provider.showInterstitial(activity)
            adDebug("Second-return opportunity consumed: shown=$shown")
        }
    }
    override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
