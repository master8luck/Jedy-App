package com.kit.jedyapp.data.remote

import com.kit.jedyapp.BuildConfig
import com.kit.jedyapp.di.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class HttpLoggingTest {
    @Test
    fun `debug logs full headers body and API key without redaction`() {
        val logs = mutableListOf<String>()
        val interceptor = createHttpLoggingInterceptor(true) { logs += it }
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setHeader("Content-Type", "application/json")
                    .setHeader("Set-Cookie", "private-cookie")
                    .setBody("""{"Title":"Movie"}""")
            )
            val request = Request.Builder()
                .url(server.url("/?s=movie&apikey=secret-key"))
                .header("Authorization", "Bearer private-token")
                .build()
            OkHttpClient.Builder().addInterceptor(interceptor).build()
                .newCall(request).execute().use {
                    assertEquals("""{"Title":"Movie"}""", it.body!!.string())
                }
        }
        val output = logs.joinToString("\n")
        assertTrue(output.contains("GET"))
        assertTrue(output.contains("Content-Type"))
        assertTrue(output.contains("Movie"))
        assertTrue(output.contains("apikey=secret-key"))
        assertFalse(output.contains("<redacted>"))
        assertTrue(output.contains("Bearer private-token"))
        assertTrue(output.contains("private-cookie"))
    }

    @Test
    fun `release emits no HTTP logs`() {
        val logs = mutableListOf<String>()
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("response"))
            val client = OkHttpClient.Builder()
                .addInterceptor(createHttpLoggingInterceptor(false) { logs += it })
                .build()
            client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        }
        assertTrue(logs.isEmpty())
    }

    @Test
    fun `production client uses the build variant logging policy`() {
        val logging = NetworkModule.provideHttpClient("test-key").interceptors
            .filterIsInstance<HttpLoggingInterceptor>().single()
        assertEquals(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE,
            logging.level,
        )
    }
}
