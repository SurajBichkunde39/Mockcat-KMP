package com.mockcat.api.http

import kotlinx.serialization.Serializable

/**
 * Portable snapshot of the HTTP response (logging / inspection).
 */
@Serializable
data class HttpResponseSnapshot(
    val statusCode: Int,
    val reasonPhrase: String? = null,
    val protocol: String? = "HTTP/1.1",
    val headers: List<HttpHeaderField> = emptyList(),
    val body: LoggedHttpBody = LoggedHttpBody.None,
)
