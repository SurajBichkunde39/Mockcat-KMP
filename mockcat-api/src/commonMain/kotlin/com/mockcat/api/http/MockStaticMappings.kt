package com.mockcat.api.http

import com.mockcat.api.MockEntry
import com.mockcat.api.MockcatResult

/**
 * Builds the effective static response for a [MockEntry]: prefers [MockEntry.staticResponse] when set,
 * otherwise derives a snapshot from legacy [MockEntry.responseCode] / [MockEntry.responseBody].
 */
fun MockEntry.effectiveStaticResponse(): HttpResponseSnapshot {
    staticResponse?.let {
        return it
    }
    return HttpResponseSnapshot(
        statusCode = responseCode ?: 200,
        reasonPhrase = null,
        protocol = "HTTP/1.1",
        headers = emptyList(),
        body =
        LoggedHttpBody.Text(
            text = responseBody.orEmpty(),
            contentType = "application/json",
        ),
    )
}

/**
 * Maps a stored response snapshot to a platform-neutral static mock result. [Content-Type] is kept on
 * [MockcatResult.ApplyStatic.contentType] for body wiring; it is stripped from [MockcatResult.ApplyStatic.responseHeaders]
 * so clients do not emit duplicate `Content-Type` lines.
 */
fun HttpResponseSnapshot.toApplyStatic(delayMs: Long): MockcatResult.ApplyStatic {
    val bodyText =
        when (val b = body) {
            is LoggedHttpBody.None -> ""
            is LoggedHttpBody.Text -> b.text
            is LoggedHttpBody.Omitted -> ""
        }
    val contentTypeFromBody =
        when (val b = body) {
            is LoggedHttpBody.Text -> b.contentType
            else -> null
        }
    val contentType =
        headers.headerValue("Content-Type")
            ?: contentTypeFromBody
            ?: "application/json"
    val headersWithoutContentType =
        headers.filterNot { it.name.equals("Content-Type", ignoreCase = true) }
    return MockcatResult.ApplyStatic(
        statusCode = statusCode,
        body = bodyText,
        contentType = contentType,
        delayMs = delayMs,
        responseHeaders = headersWithoutContentType,
    )
}
