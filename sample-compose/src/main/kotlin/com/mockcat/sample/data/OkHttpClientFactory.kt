package com.mockcat.sample.data

import android.content.Context
import com.mockcat.intercept.okhttp.MockcatIntercept
import com.mockcat.logger.okhttp.MockcatLogging
import okhttp3.OkHttpClient

object OkHttpClientFactory {
    fun create(appContext: Context): OkHttpClient {
        val httpLog = MockcatLogging(appContext)
        val mockcat = MockcatIntercept(appContext)
        return OkHttpClient.Builder()
            .addInterceptor(httpLog)
            .addInterceptor(mockcat)
            .build()
            .also { client -> mockcat.bindClient(client) }
    }
}
