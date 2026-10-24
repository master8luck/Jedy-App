package com.kit.jedyapp.data.remote

import okhttp3.logging.HttpLoggingInterceptor

internal fun createHttpLoggingInterceptor(
    debug: Boolean,
    logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT,
): HttpLoggingInterceptor = HttpLoggingInterceptor(logger).apply {
    level = if (debug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
}
