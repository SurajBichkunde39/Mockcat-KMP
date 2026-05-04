package com.mockcat.api.http

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpLoggingModelsTest {

    @Test
    fun headerValue_isCaseInsensitive() {
        val h = listOf(HttpHeaderField("X-Test", "1"), HttpHeaderField("X-Other", "2"))
        assertEquals("1", h.headerValue("x-test"))
    }

    @Test
    fun loggedHttpCall_jsonRoundTrip() {
        val call = LoggedHttpCall(
            id = 42L,
            request = HttpRequestSnapshot(
                method = "GET",
                url = "https://example.com/a?b=1",
                protocol = "HTTP/1.1",
                headers = listOf(HttpHeaderField("Accept", "text/plain")),
                body = LoggedHttpBody.Text("hi", "text/plain", 2L),
            ),
            response = HttpResponseSnapshot(
                statusCode = 200,
                reasonPhrase = "OK",
                headers = listOf(HttpHeaderField("Content-Type", "text/plain")),
                body = LoggedHttpBody.Text("ok"),
            ),
            requestTimestampMs = 1000L,
            responseTimestampMs = 1100L,
            durationMs = 100L,
            error = null,
        )
        val json = Json { ignoreUnknownKeys = true }
        val str = json.encodeToString(LoggedHttpCall.serializer(), call)
        val back = json.decodeFromString(LoggedHttpCall.serializer(), str)
        assertEquals(call, back)
    }
}
