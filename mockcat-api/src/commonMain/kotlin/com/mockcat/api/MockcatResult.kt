package com.mockcat.api

sealed class MockcatResult {
    data object PassThrough : MockcatResult()

    data class ApplyStatic(
        val statusCode: Int,
        val body: String,
        val contentType: String = "application/json",
        val delayMs: Long = 0L,
    ) : MockcatResult()

    data class Redirect(
        val targetUrl: String,
    ) : MockcatResult()

    data class Error(
        val statusCode: Int,
        val message: String,
    ) : MockcatResult()
}
