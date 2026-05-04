package com.mockcat.api.http

import kotlinx.serialization.Serializable

/**
 * One HTTP header line. Duplicates (e.g. Set-Cookie) are allowed; order is significant for display
 * and some servers. [name] and [value] are raw wire values; matching may use case-insensitive name.
 */
@Serializable
data class HttpHeaderField(
    val name: String,
    val value: String,
)

/**
 * Returns the first header value for [name], or null. Name comparison is case-insensitive.
 */
fun List<HttpHeaderField>.headerValue(name: String): String? = firstOrNull { it.name.equals(name, ignoreCase = true) }?.value
