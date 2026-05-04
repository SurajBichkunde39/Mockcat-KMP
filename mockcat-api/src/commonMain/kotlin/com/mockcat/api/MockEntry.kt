package com.mockcat.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MockEntry(
    val id: Long = 0L,
    val url: String,
    val label: String = "",
    val httpMethod: String,
    val isEnabled: Boolean = true,
    val mockType: MockType = MockType.STATIC,
    val responseCode: Int? = null,
    val responseBody: String? = null,
    val delayMs: Long? = null,
    val redirectUrl: String? = null,
    val requiredHeaders: Map<String, String>? = null,
)

@Serializable
data class MockFileEntry(
    val url: String,
    val label: String = "",
    val httpMethod: String,
    val isEnabled: Boolean = true,
    val mockType: MockType = MockType.STATIC,
    val responseCode: Int? = null,
    val responseBody: JsonElement? = null,
    val delayMs: Long? = null,
    val redirectUrl: String? = null,
    val requiredHeaders: Map<String, String>? = null,
)

@Serializable
data class MultipleMockEntries(
    val entries: List<MockFileEntry>,
)
