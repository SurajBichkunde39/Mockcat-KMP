package com.mockcat.intercept.urlsession

import com.mockcat.api.HttpRequestMetadata
import platform.Foundation.NSURLRequest

/**
 * Map an [NSURLRequest] into [HttpRequestMetadata] for [com.mockcat.api.resolveWithMatcher].
 * URL is taken from the request. Method defaults to `GET` and headers are empty in this
 * binding (NSURLRequest’s HTTP fields are not exposed in this module’s K/N API surface);
 * for full fidelity from an iOS app, build [HttpRequestMetadata] in Swift from
 * [URLRequest](https://developer.apple.com/documentation/foundation/urlrequest) and call
 * [com.mockcat.api.MockcatStore] from Kotlin, or use [runMockcatUrlSessionResolve] after
 * re-hydrating an [NSURLRequest] you control.
 */
fun NSURLRequest.toHttpRequestMetadata(): HttpRequestMetadata {
    val url = URL?.absoluteString ?: ""
    return HttpRequestMetadata(
        url = url,
        method = "GET",
        headers = emptyList(),
    )
}
