package com.mockcat.logger.ktor

import android.content.Context
import com.mockcat.logger.HttpLogReaderRegistry
import com.mockcat.logger.persistence.getHttpLogStoreForAndroid
import io.ktor.client.HttpClientConfig

fun HttpClientConfig<*>.installMockcatKtorHttpLogging(
    context: Context,
    maxResponseBodyBytes: Long = 256L * 1024L,
) {
    val app = context.applicationContext
    val store = getHttpLogStoreForAndroid(app)
    HttpLogReaderRegistry.install(store)

    install(MockcatKtorHttpLogging) {
        writer = store
        this.maxResponseBodyBytes = maxResponseBodyBytes
    }
}
