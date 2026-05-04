package com.mockcat.sample.data

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mockcat.intercept.okhttp.MockcatIntercept
import com.mockcat.logger.okhttp.MockcatLogging
import okhttp3.OkHttpClient

/**
 * **Composition root for the sample [OkHttpClient]:** Chucker, [MockcatLogging], and
 * [com.mockcat.intercept.okhttp.MockcatOkHttpInterceptor] via [MockcatIntercept] (process-wide
 * stores from logger / intercept persistence). Call [MockcatIntercept.bindClient] after building the
 * client so redirect mocks can execute inner calls.
 */
object OkHttpClientFactory {
    fun create(appContext: Context): OkHttpClient {
        val chucker = ChuckerInterceptor(appContext)
        val httpLog = MockcatLogging(appContext)
        val mockcat = MockcatIntercept(appContext)
        return OkHttpClient.Builder()
            .addInterceptor(chucker)
            .addInterceptor(httpLog)
            .addInterceptor(mockcat)
            .build()
            .also { client -> mockcat.bindClient(client) }
    }
}
