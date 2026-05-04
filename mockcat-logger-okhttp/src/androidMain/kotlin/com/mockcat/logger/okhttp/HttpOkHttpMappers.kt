package com.mockcat.logger.okhttp

import com.mockcat.api.http.HttpHeaderField
import com.mockcat.api.http.HttpRequestSnapshot
import com.mockcat.api.http.HttpResponseSnapshot
import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.http.OmissionReason
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import java.util.Collections

internal fun Headers.toHeaderFields(): List<HttpHeaderField> = Collections.unmodifiableList(
    (0 until size).map { i -> HttpHeaderField(name = name(i), value = value(i)) },
)

/**
 * v1: request **body** is not duplicated here; non-empty bodies are [LoggedHttpBody.Omitted] to avoid
 * one-shot [okhttp3.RequestBody] read issues. Follow-up can wrap the request with a replay body.
 */
internal fun Request.toHttpRequestSnapshot(
    requestBody: LoggedHttpBody,
): HttpRequestSnapshot = HttpRequestSnapshot(
    method = method,
    url = url.toString(),
    protocol = null,
    headers = headers.toHeaderFields(),
    body = requestBody,
)

internal fun Response.toHttpResponseSnapshot(
    responseBody: LoggedHttpBody,
): HttpResponseSnapshot = HttpResponseSnapshot(
    statusCode = code,
    reasonPhrase = message.ifEmpty { null },
    protocol = protocol.toString(),
    headers = headers.toHeaderFields(),
    body = responseBody,
)

/**
 * Peeks the response without consuming the original; [OmissionReason] on failure.
 */
internal fun readResponseBodyForLog(
    response: Response,
    maxBytes: Long,
): LoggedHttpBody {
    val peeked = try {
        response.peekBody(maxBytes)
    } catch (_: Exception) {
        return LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
    }
    return try {
        val text = peeked.string()
        if (text.isEmpty()) {
            LoggedHttpBody.None
        } else {
            val ct = response.body?.contentType()?.toString()
            val len = response.body?.contentLength()
            LoggedHttpBody.Text(
                text = text,
                contentType = ct,
                byteLength = len?.takeIf { it >= 0L } ?: text.length.toLong(),
            )
        }
    } catch (_: Exception) {
        LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
    }
}

internal fun requestBodyModeForLog(request: Request): LoggedHttpBody {
    val b = request.body
    if (b == null) {
        return LoggedHttpBody.None
    }
    if (b.contentLength() == 0L) {
        return LoggedHttpBody.None
    }
    return LoggedHttpBody.Omitted(OmissionReason.NOT_READ)
}
