package com.mockcat.api

import com.mockcat.api.http.HttpResponseSnapshot
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MockEntry(
    val id: Long = 0L,
    /** Base URL (scheme, host, path) without a `?` query string; use [requiredQueryParams] to match the query. */
    val url: String,
    val label: String = "",
    val httpMethod: String,
    val isEnabled: Boolean = true,
    val mockType: MockType = MockType.STATIC,
    /**
     * Rich static response when [mockType] is [MockType.STATIC]. When non-null, takes precedence over
     * [responseCode] / [responseBody].
     */
    val staticResponse: HttpResponseSnapshot? = null,
    val responseCode: Int? = null,
    val responseBody: String? = null,
    val delayMs: Long? = null,
    val redirectUrl: String? = null,
    val requiredHeaders: Map<String, String>? = null,
    /**
     * When non-null, the request’s query (parsed from the full request URL) must include these key/value pairs.
     * `null` or empty means: do not constrain the query (any or no query is accepted for the same [url] and method).
     */
    val requiredQueryParams: Map<String, String>? = null,
)

@Serializable
data class MockFileEntry(
    val url: String,
    val label: String = "",
    val httpMethod: String,
    val isEnabled: Boolean = true,
    val mockType: MockType = MockType.STATIC,
    val staticResponse: HttpResponseSnapshot? = null,
    val responseCode: Int? = null,
    val responseBody: JsonElement? = null,
    val delayMs: Long? = null,
    val redirectUrl: String? = null,
    val requiredHeaders: Map<String, String>? = null,
    val requiredQueryParams: Map<String, String>? = null,
)

@Serializable
data class MockFileEntries(
    val entries: List<MockFileEntry>,
)

@Serializable
data class MockEntries(
    val entries: List<MockEntry>,
)
