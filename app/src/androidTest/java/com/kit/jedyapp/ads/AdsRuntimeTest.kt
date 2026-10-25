package com.kit.jedyapp.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.kit.jedyapp.sdk.AdsRuntime
import com.kit.jedyapp.sdk.api.AdsProvider
import com.kit.jedyapp.sdk.api.NativeAdHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class AdsRuntimeTest {
    @Test fun secondReturnShowsOnceAndActivityResumesDoNotCountAsReturns() = onMain {
        val provider = FakeAdsProvider()
        val runtime = runtime(provider)
        val owner = TestOwner()
        val host = Activity()
        runtime.onStart(owner)
        repeat(3) { runtime.onActivityResumed(host) }
        assertEquals(0, provider.showRequests)
        runtime.onStop(owner)
        runtime.onStart(owner)
        runtime.onActivityResumed(host)
        assertEquals(0, provider.showRequests)
        assertEquals(2, provider.preloadRequests)
        runtime.onStop(owner)
        runtime.onStart(owner)
        repeat(3) { runtime.onActivityResumed(host) }
        assertEquals(1, provider.showRequests)
        runtime.onStop(owner)
        runtime.onStart(owner)
        runtime.onActivityResumed(host)
        assertEquals(1, provider.showRequests)
    }

    @Test fun unavailableAdIsNotShownLaterInTheLaunch() = onMain {
        val provider = FakeAdsProvider().apply { available = false }
        val runtime = runtime(provider)
        val owner = TestOwner()
        val host = Activity()
        repeat(3) {
            runtime.onStart(owner)
            runtime.onActivityResumed(host)
            runtime.onStop(owner)
        }
        assertEquals(1, provider.showRequests)
        provider.available = true
        runtime.onStart(owner)
        runtime.onActivityResumed(host)
        assertEquals(1, provider.showRequests)
    }

    private fun runtime(provider: AdsProvider) = AdsRuntime(
        ApplicationProvider.getApplicationContext<Application>(), provider, Activity::class.java
    )

    private fun onMain(block: () -> Unit) =
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)

    private class TestOwner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }

    private class FakeAdsProvider : AdsProvider {
        var preloadRequests = 0
        var showRequests = 0
        var available = true
        override fun initialize() = Unit
        override fun loadNative(onLoaded: (NativeAdHandle?) -> Unit) = onLoaded(null)
        override fun preloadInterstitial() { preloadRequests++ }
        override fun showInterstitial(activity: Activity): Boolean {
            showRequests++
            return available
        }
    }
}