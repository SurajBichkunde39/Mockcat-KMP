package com.mockcat.persistence

import com.mockcat.api.MockEntry
import com.mockcat.api.MockFileEntry
import kotlinx.serialization.json.JsonElement

fun MockEntity.toEntry(): MockEntry = MockEntry(
    id = id,
    url = url,
    label = label,
    httpMethod = httpMethod,
    isEnabled = isEnabled,
    mockType = mockType,
    responseCode = responseCode,
    responseBody = responseBody,
    delayMs = delayMs,
    redirectUrl = redirectUrl,
    requiredHeaders = requiredHeaders,
)

fun MockEntry.toEntity(): MockEntity = MockEntity(
    id = id,
    url = url,
    label = label,
    httpMethod = httpMethod,
    isEnabled = isEnabled,
    mockType = mockType,
    responseCode = responseCode,
    responseBody = responseBody,
    delayMs = delayMs,
    redirectUrl = redirectUrl,
    requiredHeaders = requiredHeaders,
)

fun MockFileEntry.toEntry(): MockEntry = MockEntry(
    id = 0L,
    url = url,
    label = label,
    httpMethod = httpMethod,
    isEnabled = isEnabled,
    mockType = mockType,
    responseCode = responseCode,
    responseBody = responseBody?.asJsonString(),
    delayMs = delayMs,
    redirectUrl = redirectUrl,
    requiredHeaders = requiredHeaders,
)

private fun JsonElement.asJsonString(): String = toString()
