package com.mockcat.sample.data

/**
 * User-selected network stack. OkHttp is wired with Mockcat interceptors and the HTTP logger.
 * Ktor uses [KtorClientFactory]: engine-agnostic mock plugin plus Ktor HTTP logging,
 * sharing [com.mockcat.persistence.ProcessMockcatStore] with OkHttp.
 */
enum class ClientKind {
    OkHttp,
    Ktor,
}
