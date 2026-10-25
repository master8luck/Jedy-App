package com.kit.jedyapp.sdk

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.policy.onForeground
import com.kit.jedyapp.sdk.policy.ReturnState

/** App-process lifetime owner. Does not retain host Activities. */
class AdsRuntime(
    private val application: Application,
    private val provider: AdsProvider,
    private val hostClass: Class<out Activity>,
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    // A new Application/process starts with fresh history; never persist ad consumption.
    private var returnState = ReturnState()
    private var eligibleThisReturn = false
    private var started = false

    fun start() {
        if (started) return
        started = true
        provider.initialize()
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        val decision = onForeground(returnState)
        returnState = decision.state
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
    override fun onActivityDestroyed(activity: Activity) {
        // Explicitly closing the host also ends a launch, even if Android keeps its process.
        // Configuration changes must preserve the current launch's counter.
        if (hostClass.isInstance(activity) && activity.isFinishing && !activity.isChangingConfigurations) {
            returnState = ReturnState()
            eligibleThisReturn = false
            adDebug("Host closed: return counter reset for the next launch")
        }
    }
}
