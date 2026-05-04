package com.mockcat.sample.data

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mockcat.intercept.okhttp.MockcatOkHttpInterceptor
import com.mockcat.persistence.getMockcatStoreForAndroid
import okhttp3.OkHttpClient

/**
 * **Composition root for Mockcat in the sample app:** builds an [OkHttpClient] with Chucker and
 * [MockcatOkHttpInterceptor] only. A [com.mockcat.api.MockcatStore] is still required for the
 * interceptor to resolve rules; it is created here via [getMockcatStoreForAndroid] and is not
 * part of the app’s feature code. Host apps that want a stricter API can add a small facade
 * module later; the interceptor API itself is “store in, OkHttp out.”
 */
object OkHttpClientFactory {
    fun create(appContext: Context): OkHttpClient {
        val store = getMockcatStoreForAndroid(appContext)
        val chucker = ChuckerInterceptor(appContext)
        val mockcat = MockcatOkHttpInterceptor(store)
        return OkHttpClient.Builder()
            .addInterceptor(chucker)
            .addInterceptor(mockcat)
            .build()
            .also { client -> mockcat.setClient(client) }
    }
}
