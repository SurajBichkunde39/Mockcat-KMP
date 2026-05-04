package com.mockcat.logger.ktor

import com.mockcat.api.http.HttpHeaderField
import com.mockcat.api.http.HttpRequestSnapshot
import com.mockcat.api.http.HttpResponseSnapshot
import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.http.OmissionReason
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent

/**
 * v1: request **body** matches the OkHttp path — non-empty or non-trivial content is
 * [LoggedHttpBody.Omitted] with [OmissionReason.NOT_READ].
 */
internal fun OutgoingContent?.toRequestBodyForLog(): LoggedHttpBody {
    this ?: return LoggedHttpBody.None
    if (this is OutgoingContent.NoContent) {
        return LoggedHttpBody.None
    }
    if (this is OutgoingContent.ByteArrayContent) {
        return if (this.bytes().isEmpty()) {
            LoggedHttpBody.None
        } else {
            LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
        }
    }
    return LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
}

/**
 * Bodies on the wire may be represented as non-[OutgoingContent] in [onRequest] (Ktor 3+).
 */
internal fun Any.toRequestBodyForLogAsOutgoing(): LoggedHttpBody = when (this) {
    is OutgoingContent -> this.toRequestBodyForLog()
    else -> LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
}

/**
 * [HttpRequestBuilder] header API differs by Ktor version: map via [io.ktor.http.HttpHeaders] entries
 * on the public request headers container.
 */
internal fun HttpRequestBuilder.toHttpRequestSnapshot(
    requestBody: LoggedHttpBody,
): HttpRequestSnapshot = HttpRequestSnapshot(
    method = this.method.value,
    url = this.url.buildString(),
    protocol = null,
    headers = this.headersMapForLog(),
    body = requestBody,
)

/**
 * [HttpRequestBuilder] exposes a headers builder: build a [Headers] snapshot, then convert.
 */
private fun HttpRequestBuilder.headersMapForLog(): List<HttpHeaderField> = this.headers.build().toHeaderFields()

internal fun HttpResponse.toHttpResponseSnapshot(
    responseBody: LoggedHttpBody,
): HttpResponseSnapshot = HttpResponseSnapshot(
    statusCode = status.value,
    reasonPhrase = status.description.takeIf { it.isNotEmpty() },
    protocol = version.toString(),
    headers = this.headers.toHeaderFields(),
    body = responseBody,
)

internal fun Headers.toHeaderFields(): List<HttpHeaderField> = entries().flatMap { (name, values) -> values.map { v -> HttpHeaderField(name, v) } }

/**
 * [HttpResponse] content type for the log: prefer header, no dependency on
 * [io.ktor.util.content] reflection on the response type.
 */
internal fun HttpResponse.contentTypeForLog(): String? = headers[HttpHeaders.ContentType]

@Suppress("TooGenericExceptionCaught")
internal fun readResponseBytesForLog(
    bytes: ByteArray,
    maxRead: Long,
    contentType: String?,
): LoggedHttpBody = try {
    if (bytes.isEmpty()) {
        LoggedHttpBody.None
    } else {
        if (maxRead in 1 until Long.MAX_VALUE && bytes.size > maxRead) {
            LoggedHttpBody.Omitted(OmissionReason.TOO_LARGE)
        } else {
            val text = bytes.decodeToString()
            LoggedHttpBody.Text(
                text = text,
                contentType = contentType,
                byteLength = text.length.toLong(),
            )
        }
    }
} catch (_: Exception) {
    LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
}
