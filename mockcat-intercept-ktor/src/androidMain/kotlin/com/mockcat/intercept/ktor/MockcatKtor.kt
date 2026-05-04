package com.mockcat.intercept.ktor

import com.mockcat.api.MockcatStore
import com.mockcat.intercept.okhttp.MockcatOkHttpInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object MockcatKtor {
    /**
     * Ktor [HttpClient] on OkHttp with Mockcat. Redirect mocks use the same engine [OkHttpClient] for the follow-up
     * request, with [X-Mockcat-Redirected] to avoid re-interception loops.
     */
    @Suppress("MagicNumber")
    fun createHttpClient(
        store: MockcatStore,
        configure: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient {
        val mockcat = MockcatOkHttpInterceptor(store)
        val engineClient =
            OkHttpClient.Builder()
                .addInterceptor(mockcat)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        mockcat.setClient(engineClient)
        return HttpClient(OkHttp) {
            engine { preconfigured = engineClient }
            configure()
        }
    }
}
