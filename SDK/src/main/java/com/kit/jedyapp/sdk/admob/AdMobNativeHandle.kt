package com.kit.jedyapp.sdk.admob

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.kit.jedyapp.sdk.R
import com.kit.jedyapp.sdk.api.NativeAdHandle

internal class AdMobNativeHandle(private val ad: NativeAd) : NativeAdHandle {
    private var closed = false

    override fun createView(context: Context, textColor: Int, surfaceColor: Int): View {
        val root = NativeAdView(context)
        val density = context.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(surfaceColor)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        fun label(text: String?, size: Float, bold: Boolean = false): TextView =
            TextView(context).apply {
                this.text = text
                textSize = size
                setTextColor(textColor)
                if (bold) setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(4), dp(28), dp(4))
                column.addView(this)
            }
        root.addView(column)
        label(context.getString(R.string.ad_attribution), 12f, true)
        root.headlineView = label(ad.headline, 18f, true)
        ad.advertiser?.let { root.advertiserView = label(it, 12f) }
        ad.icon?.drawable?.let { drawable ->
            val icon = ImageView(context).apply {
                setImageDrawable(drawable)
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
            }
            column.addView(icon)
            root.iconView = icon
        }
        val media = MediaView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(160))
            mediaContent = ad.mediaContent
        }
        column.addView(media)
        root.mediaView = media
        ad.body?.let { root.bodyView = label(it, 14f) }
        ad.callToAction?.let { callToAction ->
            val button = Button(context).apply { text = callToAction }
            column.addView(button)
            root.callToActionView = button
        }
        root.setNativeAd(ad)
        return root
    }

    override fun close() {
        if (!closed) { closed = true; ad.destroy() }
    }
}
