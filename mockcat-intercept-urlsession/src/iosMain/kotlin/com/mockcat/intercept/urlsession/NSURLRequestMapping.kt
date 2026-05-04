package com.mockcat.intercept.urlsession

import com.mockcat.api.HttpRequestMetadata
import platform.Foundation.NSURLRequest

/**
 * Map an [NSURLRequest] into [HttpRequestMetadata] for [com.mockcat.api.resolveWithMatcher].
 * In an [platform.Foundation.NSURLProtocol] implementation, call this from
 * [platform.Foundation.NSURLProtocol.startLoading] and then deliver a matching
 * [platform.Foundation.NSHTTPURLResponse] + body, or use [com.mockcat.api.MockcatResult]
 * to decide pass-through to the next protocol handler.
 */
fun NSURLRequest.toHttpRequestMetadata(): HttpRequestMetadata {
    val url = URL?.absoluteString ?: ""
    val method = HTTPMethod ?: "GET"
    // Header enumeration varies by request mutability; extend here if you need stricter [requiredHeaders] matching.
    return HttpRequestMetadata(
        url = url,
        method = method,
        headers = emptyList(),
    )
}
