package com.mockcat.intercept.urlsession

import com.mockcat.api.MockcatResult

/**
 * Single-value map for outbound mock headers (later entries win). Use from Swift `URLProtocol`
 * when building `HTTPURLResponse` header dictionaries.
 */
fun MockcatResult.ApplyStatic.flattenedResponseHeaders(): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    for (h in responseHeaders) {
        out[h.name] = h.value
    }
    return out
}
