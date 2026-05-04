package com.mockcat.android.okhttp

import android.content.Context
import com.mockcat.logger.HttpLogReader
import com.mockcat.logger.okhttp.MockcatHttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Response

class MockcatLogging(
    context: Context,
) : Interceptor {
    private val delegate: MockcatHttpLoggingInterceptor = MockcatHttpLoggingInterceptor(
        writer = AndroidHttpLogStoreHolder.get(context),
    )

    override fun intercept(chain: Interceptor.Chain): Response = delegate.intercept(chain)

    companion object {
        fun logReader(context: Context): HttpLogReader = AndroidHttpLogStoreHolder.get(context)
    }
}
