package com.mockcat.intercept.ktor

import android.content.Context
import com.mockcat.api.MockcatStore
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin

class MockcatKtorInterceptConfig {
    var store: MockcatStore? = null
}

val MockcatKtorIntercept = createClientPlugin("MockcatKtorIntercept", ::MockcatKtorInterceptConfig) {
    // no-op: mock interception is disabled in non-debug builds
}

@Suppress("UnusedParameter")
fun HttpClientConfig<*>.installMockcatKtorIntercept(context: Context) = Unit
