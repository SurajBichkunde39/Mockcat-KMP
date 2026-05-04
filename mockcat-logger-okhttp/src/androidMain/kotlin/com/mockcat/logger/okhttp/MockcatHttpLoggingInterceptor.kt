package com.mockcat.logger.okhttp

import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogWriter
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Read-only: records [LoggedHttpCall] to [writer] and returns the [Response] from the chain unmodified.
 * Request bodies are not read (v1) when present — see [requestBodyModeForLog]. Response bodies use
 * [okhttp3.Response.peekBody] up to [maxResponseBodyBytes].
 */
class MockcatHttpLoggingInterceptor(
    private val writer: HttpLogWriter,
    private val maxResponseBodyBytes: Long = 256L * 1024L,
) : Interceptor {

    @Suppress("TooGenericExceptionCaught")
    override fun intercept(chain: Interceptor.Chain): Response {
        val callStart = System.currentTimeMillis()
        val request = chain.request()
        val requestSnapshot = request.toHttpRequestSnapshot(requestBodyModeForLog(request))
        return try {
            val response = chain.proceed(request)
            val end = System.currentTimeMillis()
            val body = readResponseBodyForLog(response, maxResponseBodyBytes)
            val responseSnapshot = response.toHttpResponseSnapshot(body)
            val logged =
                LoggedHttpCall(
                    id = 0L,
                    request = requestSnapshot,
                    response = responseSnapshot,
                    requestTimestampMs = callStart,
                    responseTimestampMs = end,
                    durationMs = end - callStart,
                    error = null,
                )
            runBlocking { writer.emit(logged) }
            response
        } catch (e: IOException) {
            val end = System.currentTimeMillis()
            val failure =
                LoggedHttpCall(
                    id = 0L,
                    request = requestSnapshot,
                    response = null,
                    requestTimestampMs = callStart,
                    responseTimestampMs = end,
                    durationMs = end - callStart,
                    error = e.message ?: e::class.simpleName,
                )
            runBlocking { writer.emit(failure) }
            throw e
        }
    }
}
