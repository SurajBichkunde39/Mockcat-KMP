package com.mockcat.logger.okhttp

import android.content.Context
import com.mockcat.logger.HttpLogReader
import com.mockcat.logger.persistence.ProcessHttpLogStore
import okhttp3.Interceptor
import okhttp3.Response

class MockcatLogging(
    context: Context,
) : Interceptor {
    private val delegate: MockcatHttpLoggingInterceptor =
        MockcatHttpLoggingInterceptor(
            writer = ProcessHttpLogStore.get(context),
        )

    override fun intercept(chain: Interceptor.Chain): Response = delegate.intercept(chain)

    companion object {
        fun logReader(context: Context): HttpLogReader = ProcessHttpLogStore.get(context)
    }
}
