package com.mockcat.api

import com.mockcat.api.http.HttpHeaderField

sealed class MockcatResult {
    data object PassThrough : MockcatResult()

    data class ApplyStatic(
        val statusCode: Int,
        val body: String,
        val contentType: String = "application/json",
        val delayMs: Long = 0L,
        /** Outbound response headers (excluding `Content-Type`, which is represented by [contentType] for body wiring). */
        val responseHeaders: List<HttpHeaderField> = emptyList(),
    ) : MockcatResult()

    data class Redirect(
        val targetUrl: String,
    ) : MockcatResult()

    data class Error(
        val statusCode: Int,
        val message: String,
    ) : MockcatResult()
}
