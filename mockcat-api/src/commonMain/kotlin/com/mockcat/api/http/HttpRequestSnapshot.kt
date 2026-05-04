package com.mockcat.api.http

import kotlinx.serialization.Serializable

/**
 * Portable snapshot of the outbound HTTP request (logging / inspection). Not for URL matching rules;
 * for that use [com.mockcat.api.HttpRequestMetadata].
 */
@Serializable
data class HttpRequestSnapshot(
    val method: String,
    /** Full URL string including `?` query if present. */
    val url: String,
    /** e.g. `HTTP/1.1` or `h2` when known. */
    val protocol: String? = null,
    val headers: List<HttpHeaderField> = emptyList(),
    val body: LoggedHttpBody = LoggedHttpBody.None,
)
