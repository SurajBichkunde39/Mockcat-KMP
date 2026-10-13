package com.mockcat.sample.data

import android.content.Context
import com.mockcat.intercept.ktor.installMockcatKtorIntercept
import com.mockcat.logger.ktor.installMockcatKtorHttpLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientFactory {
    /**
     * Ktor [HttpClient] (OkHttp engine) with JSON, mock resolution ([installMockcatKtorIntercept]), then HTTP logging
     * ([installMockcatKtorHttpLogging]) so logs reflect the final status and body (including static mocks).
     */
    fun create(appContext: Context): HttpClient = HttpClient(OkHttp) {
        installMockcatKtorIntercept(appContext)
        installMockcatKtorHttpLogging(appContext)
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    }
}
