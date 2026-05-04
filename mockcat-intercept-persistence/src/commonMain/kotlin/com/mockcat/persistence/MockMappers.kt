package com.mockcat.persistence

import com.mockcat.api.MockEntry
import com.mockcat.api.MockFileEntry
import com.mockcat.api.splitHttpUrl
import kotlinx.serialization.json.JsonElement

fun MockEntity.toEntry(): MockEntry = MockEntry(
    id = id,
    url = url,
    label = label,
    httpMethod = httpMethod,
    isEnabled = isEnabled,
    mockType = mockType,
    staticResponse = staticResponse,
    responseCode = responseCode,
    responseBody = responseBody,
    delayMs = delayMs,
    redirectUrl = redirectUrl,
    requiredHeaders = requiredHeaders,
    requiredQueryParams = requiredQueryParams,
)

fun MockEntry.toEntity(): MockEntity = MockEntity(
    id = id,
    url = url,
    label = label,
    httpMethod = httpMethod,
    isEnabled = isEnabled,
    mockType = mockType,
    staticResponse = staticResponse,
    responseCode = responseCode,
    responseBody = responseBody,
    delayMs = delayMs,
    redirectUrl = redirectUrl,
    requiredHeaders = requiredHeaders,
    requiredQueryParams = requiredQueryParams,
)

fun MockFileEntry.toEntry(): MockEntry {
    val (base, fromUrl) = splitHttpUrl(url)
    val explicit = requiredQueryParams
    val mergedQuery: Map<String, String>? =
        when {
            explicit != null && explicit.isNotEmpty() -> explicit
            explicit != null && explicit.isEmpty() -> null
            fromUrl.isNotEmpty() -> fromUrl
            else -> null
        }
    return MockEntry(
        id = 0L,
        url = base,
        label = label,
        httpMethod = httpMethod,
        isEnabled = isEnabled,
        mockType = mockType,
        staticResponse = staticResponse,
        responseCode = responseCode,
        responseBody = responseBody?.asJsonString(),
        delayMs = delayMs,
        redirectUrl = redirectUrl,
        requiredHeaders = requiredHeaders,
        requiredQueryParams = mergedQuery,
    )
}

private fun JsonElement.asJsonString(): String = toString()
