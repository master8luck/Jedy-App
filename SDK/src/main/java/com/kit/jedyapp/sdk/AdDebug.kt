package com.kit.jedyapp.sdk

import android.util.Log

/** Ad lifecycle diagnostics are disabled in release builds. */
internal fun adDebug(message: String) {
    if (BuildConfig.DEBUG) Log.d("JedyAds", message)
}
