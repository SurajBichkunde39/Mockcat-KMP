package com.mockcat.intercept.urlsession

import com.mockcat.api.MockcatResult
import com.mockcat.api.MockcatStore
import com.mockcat.api.resolveWithMatcher
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSURLRequest

/**
 * Synchronous [MockcatStore] resolution for URL loading integration (e.g. Swift
 * [URLProtocol](https://developer.apple.com/documentation/foundation/urlprotocol)).
 * Prefer calling from a **background** queue: [MockcatStore] uses suspend, so this
 * [runBlocking] can block; never block the UI thread in production.
 */
fun runMockcatUrlSessionResolve(request: NSURLRequest, store: MockcatStore): MockcatResult = runBlocking { store.resolveWithMatcher(request.toHttpRequestMetadata()) }

// How to use from Swift: see AGENT.md — `RunMockcatUrlSessionResolve` + a Swift `URLProtocol`
// with `getMockcatStoreForIos()` and the store passed from the app.
