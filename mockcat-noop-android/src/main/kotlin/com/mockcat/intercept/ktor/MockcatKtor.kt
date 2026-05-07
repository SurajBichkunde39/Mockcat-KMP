package com.mockcat.intercept.ktor

import com.mockcat.api.MockcatStore
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

object MockcatKtor {
    fun createHttpClient(
        @Suppress("UnusedPrivateParameter")
        store: MockcatStore,
        configure: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient = HttpClient(OkHttp) { configure() }
}
