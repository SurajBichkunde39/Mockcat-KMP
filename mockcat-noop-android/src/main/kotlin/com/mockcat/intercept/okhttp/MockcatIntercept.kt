package com.mockcat.intercept.okhttp

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class MockcatIntercept(
    @Suppress("UnusedPrivateProperty")
    private val context: Context,
) : Interceptor {
    fun bindClient(@Suppress("UnusedPrivateProperty") client: OkHttpClient) = Unit

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
