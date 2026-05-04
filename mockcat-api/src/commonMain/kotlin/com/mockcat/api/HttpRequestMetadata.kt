package com.mockcat.api

/**
 * Portable snapshot of a single outbound HTTP call for mock matching and rules.
 * Header keys are compared in a case-insensitive way in [headerValue].
 */
data class HttpRequestMetadata(
    val url: String,
    val method: String,
    val headers: List<Pair<String, String>> = emptyList(),
) {
    fun headerValue(name: String): String? = headers.find { (k, _) -> k.equals(name, ignoreCase = true) }?.second
}

object MockcatHeaders {
    const val REDIRECT_MARKER = "X-Mockcat-Redirected"
    const val SOURCE_NAME = "X-Mockcat-Source"
    const val SOURCE_VALUE_STATIC = "static-mock"
    const val SOURCE_VALUE_REDIRECT = "redirected-response"
    const val SOURCE_VALUE_ERROR = "redirect-error"
}
