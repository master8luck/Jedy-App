package com.kit.jedyapp.di

import android.app.Application
import android.content.Context
import com.kit.jedyapp.MainActivity
import com.kit.jedyapp.sdk.AdsRuntime
import com.kit.jedyapp.sdk.admob.AdMobProvider
import com.kit.jedyapp.sdk.api.AdsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideAdsProvider(@ApplicationContext context: Context): AdsProvider = AdMobProvider(context)

    @Provides
    @Singleton
    fun provideAdsRuntime(@ApplicationContext context: Context, provider: AdsProvider): AdsRuntime =
        AdsRuntime(context as Application, provider, MainActivity::class.java)
}
