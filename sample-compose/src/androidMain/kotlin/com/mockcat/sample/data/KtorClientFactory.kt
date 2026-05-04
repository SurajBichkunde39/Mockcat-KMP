package com.mockcat.sample.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClientFactory {
    /**
     * Plain Ktor [HttpClient] (OkHttp engine) with JSON; no Mockcat, Chucker, or HTTP log wiring.
     */
    fun create(): HttpClient = HttpClient(OkHttp) {
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
