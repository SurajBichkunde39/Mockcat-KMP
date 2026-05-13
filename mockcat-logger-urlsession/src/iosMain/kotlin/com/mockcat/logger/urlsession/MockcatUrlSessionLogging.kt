package com.mockcat.logger.urlsession

import com.mockcat.api.http.HttpHeaderField
import com.mockcat.api.http.HttpRequestSnapshot
import com.mockcat.api.http.HttpResponseSnapshot
import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.api.http.OmissionReason
import com.mockcat.logger.HttpLogReaderRegistry
import com.mockcat.logger.persistence.getHttpLogStoreForIos
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.valueForKey
import platform.posix.memcpy

/**
 * Minimal host API for URLSession logging.
 *
 * Call [mockcatUrlSessionNowMs] before starting a task, and then [mockcatUrlSessionLogCall]
 * from the URLSession completion handler.
 *
 * v1 behavior matches OkHttp/Ktor:
 * - request bodies are omitted (not read)
 * - response bodies are logged up to [maxResponseBodyBytes]
 */
fun mockcatUrlSessionNowMs(): Long = (NSProcessInfo.processInfo.systemUptime * 1000.0).toLong()

private val loggerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

fun mockcatUrlSessionLogCall(
    request: NSURLRequest,
    response: NSURLResponse?,
    data: NSData?,
    error: NSError?,
    requestTimestampMs: Long,
    maxResponseBodyBytes: Long = 256L * 1024L,
) {
    val store = getHttpLogStoreForIos().also { HttpLogReaderRegistry.install(it) }
    val t1 = mockcatUrlSessionNowMs()

    val requestSnapshot = request.toHttpRequestSnapshot()
    val responseSnapshot = response.toHttpResponseSnapshot(
        bytes = data?.toByteArrayCapped(maxResponseBodyBytes),
        maxBytes = maxResponseBodyBytes,
    )

    val logged =
        LoggedHttpCall(
            id = 0L,
            request = requestSnapshot,
            response = responseSnapshot,
            requestTimestampMs = requestTimestampMs,
            responseTimestampMs = t1,
            durationMs = t1 - requestTimestampMs,
            error = error?.localizedDescription,
        )

    loggerScope.launch {
        store.emit(logged)
    }
}

private fun NSURLRequest.toHttpRequestSnapshot(): HttpRequestSnapshot {
    val method = (valueForKey("HTTPMethod") as? String) ?: "GET"
    val url = URL?.absoluteString ?: ""

    @Suppress("UNCHECKED_CAST")
    val headersDict = valueForKey("allHTTPHeaderFields") as? Map<Any?, Any?>
    val headers =
        headersDict
            ?.entries
            ?.mapNotNull { (k, v) ->
                val key = k?.toString() ?: return@mapNotNull null
                val value = v?.toString() ?: return@mapNotNull null
                HttpHeaderField(name = key, value = value)
            }
            ?: emptyList()

    val body =
        if (valueForKey("HTTPBody") == null) {
            LoggedHttpBody.None
        } else {
            LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
        }

    return HttpRequestSnapshot(
        method = method,
        url = url,
        protocol = null,
        headers = headers,
        body = body,
    )
}

private fun NSURLResponse?.toHttpResponseSnapshot(bytes: ByteArray?, maxBytes: Long): HttpResponseSnapshot? {
    if (this == null) return null
    val http = this as? NSHTTPURLResponse
    val status = http?.statusCode?.toInt() ?: 0

    @Suppress("UNCHECKED_CAST")
    val headersDict = http?.valueForKey("allHeaderFields") as? Map<Any?, Any?>
    val headers =
        headersDict
            ?.entries
            ?.mapNotNull { (k, v) ->
                val key = k?.toString() ?: return@mapNotNull null
                val value = v?.toString() ?: return@mapNotNull null
                HttpHeaderField(name = key, value = value)
            }
            ?: emptyList()

    val ct = headers.firstOrNull { it.name.equals("Content-Type", ignoreCase = true) }?.value
    val body =
        when {
            bytes == null || bytes.isEmpty() -> LoggedHttpBody.None
            maxBytes in 1 until Long.MAX_VALUE && bytes.size.toLong() > maxBytes ->
                LoggedHttpBody.Omitted(OmissionReason.TOO_LARGE)
            else ->
                LoggedHttpBody.Text(
                    text = bytes.decodeToString(),
                    contentType = ct,
                    byteLength = bytes.size.toLong(),
                )
        }

    return HttpResponseSnapshot(
        statusCode = status,
        reasonPhrase = null,
        protocol = null,
        headers = headers,
        body = body,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArrayCapped(maxBytes: Long): ByteArray {
    val len = length.toLong()
    if (len <= 0L) return ByteArray(0)
    val toRead = minOf(len, maxBytes).toInt()
    val out = ByteArray(toRead)
    val src = bytes ?: return ByteArray(0)
    out.usePinned { pinned ->
        memcpy(pinned.addressOf(0), src, toRead.convert())
    }
    return out
}
