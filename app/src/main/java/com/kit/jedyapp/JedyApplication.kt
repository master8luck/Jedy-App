package com.kit.jedyapp

import android.app.Application
import com.kit.jedyapp.sdk.AdsRuntime
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class JedyApplication : Application() {
    @Inject lateinit var adsRuntime: AdsRuntime
    override fun onCreate() {
        super.onCreate()
        adsRuntime.start()
    }
}
