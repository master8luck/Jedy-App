package com.kit.jedyapp.di

import com.kit.jedyapp.BuildConfig
import com.kit.jedyapp.data.remote.OmdbApi
import com.kit.jedyapp.data.remote.createHttpLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("omdbApiKey")
    fun provideApiKey(): String = BuildConfig.OMDB_API_KEY

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideHttpClient(@Named("omdbApiKey") apiKey: String): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                if (apiKey.isBlank()) {
                    throw IOException("Set OMDB_API_KEY in local.properties and rebuild.")
                }
                val url = chain.request().url.newBuilder()
                    .addQueryParameter("apikey", apiKey)
                    .build()
                chain.proceed(chain.request().newBuilder().url(url).build())
            }
            .addInterceptor(createHttpLoggingInterceptor(BuildConfig.DEBUG))
            .build()

    @Provides
    @Singleton
    fun provideOmdbApi(client: OkHttpClient, json: Json): OmdbApi =
        Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OmdbApi::class.java)

}
