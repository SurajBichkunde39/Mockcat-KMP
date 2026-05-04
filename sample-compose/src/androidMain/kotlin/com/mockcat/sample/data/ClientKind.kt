package com.mockcat.sample.data

/**
 * User-selected network stack. OkHttp is wired with Chucker, Mockcat interceptors, and
 * [com.mockcat.logger.ui.MockcatLoggerUi] HTTP log. Ktor uses a plain [io.ktor.client.HttpClient] only.
 */
enum class ClientKind {
    OkHttp,
    Ktor,
}
