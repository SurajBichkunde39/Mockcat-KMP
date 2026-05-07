package com.mockcat.intercept.okhttp

import com.mockcat.api.MockcatStore
import okhttp3.Interceptor
import okhttp3.Response

class MockcatOkHttpInterceptor(
    @Suppress("UnusedPrivateProperty")
    private val store: MockcatStore,
) : Interceptor {
    fun setClient(okHttpClient: okhttp3.OkHttpClient) = Unit

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
