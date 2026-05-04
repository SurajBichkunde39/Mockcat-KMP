package com.mockcat.intercept.okhttp

import com.mockcat.api.HttpRequestMetadata
import okhttp3.Request

internal fun Request.toRequestMetadata(): HttpRequestMetadata = HttpRequestMetadata(
    url = url.toString(),
    method = method,
    headers = (0 until headers.size).map { i -> headers.name(i) to headers.value(i) },
)
