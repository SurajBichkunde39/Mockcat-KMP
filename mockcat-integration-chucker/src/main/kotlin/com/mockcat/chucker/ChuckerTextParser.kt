package com.mockcat.chucker

import com.mockcat.api.MockEntry
import com.mockcat.api.MockType

class ChuckerParseException(message: String) : Exception(message)

object ChuckerTextParser {
    fun toMock(chuckerText: String): MockEntry {
        val lines = chuckerText.lineSequence()
        val url =
            lines.find { it.startsWith("URL:") }?.substringAfter("URL:")?.trim()
                ?: throw ChuckerParseException("URL not found in export.")
        val method =
            lines.find { it.startsWith("Method:") }?.substringAfter("Method:")?.trim()
                ?: throw ChuckerParseException("Method not found in export.")
        val responseCode =
            lines.find { it.startsWith("Response:") }?.substringAfter("Response:")?.trim()
                ?.split(" ")?.firstOrNull()?.toIntOrNull()
                ?: throw ChuckerParseException("Response code not found or invalid.")
        val responseSectionDelimiter = "---------- Response ----------"
        val responseSectionStartIndex = chuckerText.indexOf(responseSectionDelimiter)
        if (responseSectionStartIndex == -1) {
            throw ChuckerParseException("Could not find the '$responseSectionDelimiter' delimiter.")
        }
        val bodyStartIndex = chuckerText.indexOf("\n\n{", startIndex = responseSectionStartIndex)
        if (bodyStartIndex == -1) {
            throw ChuckerParseException("Could not find the blank line separator between response headers and body.")
        }
        val responseBody = chuckerText.substring(bodyStartIndex).trim()
        if (responseBody.isBlank() || responseBody == "(body is empty)") {
            throw ChuckerParseException("Cannot import a transaction with an empty or omitted response body.")
        }
        return MockEntry(
            url = url,
            label = "Imported from Chucker @${System.currentTimeMillis()}",
            httpMethod = method,
            responseCode = responseCode,
            responseBody = responseBody,
            delayMs = 200L,
            isEnabled = true,
            mockType = MockType.STATIC,
        )
    }
}
