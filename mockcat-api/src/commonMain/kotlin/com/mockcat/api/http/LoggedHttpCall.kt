package com.mockcat.api.http

import kotlinx.serialization.Serializable

/**
 * A single HTTP exchange: one request, optional response, timing, and optional error (e.g. network
 * failure before a response was received).
 */
@Serializable
data class LoggedHttpCall(
    val id: Long = 0L,
    val request: HttpRequestSnapshot,
    val response: HttpResponseSnapshot? = null,
    val requestTimestampMs: Long? = null,
    val responseTimestampMs: Long? = null,
    val durationMs: Long? = null,
    val error: String? = null,
)
