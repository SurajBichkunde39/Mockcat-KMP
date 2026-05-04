package com.mockcat.api

/**
 * Splits a URL string on the first `?` into a base (no query) and
 * query key/value map. Duplicate keys keep the last value.
 * Decodes `+` as space and percent-encoded bytes in the query part (single-byte pairs).
 */
fun splitHttpUrl(url: String): Pair<String, Map<String, String>> {
    if (url.isEmpty()) {
        return "" to emptyMap()
    }
    val q = url.indexOf('?')
    if (q < 0) {
        return url to emptyMap()
    }
    val base = url.substring(0, q)
    val query = url.substring(q + 1)
    return base to parseQueryString(query)
}

/**
 * Base URL without the `?` query part (or fragment) — [HttpRequestMetadata.url] is the full string.
 */
val HttpRequestMetadata.baseUrl: String
    get() = splitHttpUrl(url).first

/**
 * Parsed query parameters from [HttpRequestMetadata.url] (order-independent; keys unique).
 */
val HttpRequestMetadata.queryParameters: Map<String, String>
    get() = splitHttpUrl(url).second

internal fun parseQueryString(query: String): Map<String, String> {
    if (query.isEmpty()) {
        return emptyMap()
    }
    val out = linkedMapOf<String, String>()
    for (part in query.split('&')) {
        if (part.isEmpty()) {
            continue
        }
        val eq = part.indexOf('=')
        if (eq < 0) {
            out[decodeFormComponent(part)] = ""
        } else {
            val k = decodeFormComponent(part.substring(0, eq))
            val v = decodeFormComponent(part.substring(eq + 1))
            out[k] = v
        }
    }
    return out
}

/**
 * Rebuilds a full URL for display/editing: [baseUrl] and optional [requiredQueryParams] (keys sorted).
 */
fun buildHttpUrl(
    baseUrl: String,
    requiredQueryParams: Map<String, String>?,
): String {
    if (requiredQueryParams.isNullOrEmpty()) {
        return baseUrl
    }
    val q =
        requiredQueryParams.entries
            .sortedBy { it.key }
            .joinToString("&") { (k, v) -> "${encodeFormComponent(k)}=${encodeFormComponent(v)}" }
    return "$baseUrl?$q"
}

private fun decodeFormComponent(s: String): String {
    val out = StringBuilder()
    var i = 0
    while (i < s.length) {
        if (s[i] == '%' && i + 2 < s.length) {
            val h = s.substring(i + 1, i + 3).toIntOrNull(16)
            if (h != null) {
                out.append((h and 0xFF).toChar())
                i += 3
                continue
            }
        }
        if (s[i] == '+') {
            out.append(' ')
        } else {
            out.append(s[i])
        }
        i++
    }
    return out.toString()
}

private fun encodeFormComponent(s: String): String = buildString {
    for (c in s) {
        when {
            c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '-' || c == '_' || c == '.' || c == '~' -> append(c)
            c == ' ' -> append('+')
            else ->
                c.toString().encodeToByteArray().forEach { b ->
                    append('%')
                    append(
                        b.toUByte()
                            .toString(16)
                            .padStart(2, '0')
                            .uppercase(),
                    )
                }
        }
    }
}
