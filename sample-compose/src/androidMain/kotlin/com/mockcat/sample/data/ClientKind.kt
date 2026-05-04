package com.mockcat.sample.data

/**
 * User-selected network stack. OkHttp is wired with Chucker, Mockcat interceptors, and
 * [com.mockcat.logger.ui.MockcatLoggerUi] HTTP log. Ktor uses [com.mockcat.sample.data.KtorClientFactory]:
 * engine-agnostic mock plugin plus Ktor HTTP logging, sharing [com.mockcat.persistence.ProcessMockcatStore] with OkHttp.
 */
enum class ClientKind {
    OkHttp,
    Ktor,
}
