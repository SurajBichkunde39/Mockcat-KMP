package com.mockcat.logger.ktor

import android.content.Context
import com.mockcat.logger.HttpLogWriter
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin

class MockcatKtorHttpLoggingConfig {
    var writer: HttpLogWriter? = null
    var maxResponseBodyBytes: Long = 256L * 1024L
}

val MockcatKtorHttpLogging = createClientPlugin("MockcatKtorHttpLogging", ::MockcatKtorHttpLoggingConfig) {
    // no-op: HTTP logging is disabled in non-debug builds
}

@Suppress("UnusedPrivateParameter", "UnusedParameter")
fun HttpClientConfig<*>.installMockcatKtorHttpLogging(
    context: Context,
    maxResponseBodyBytes: Long = 256L * 1024L,
) = Unit
