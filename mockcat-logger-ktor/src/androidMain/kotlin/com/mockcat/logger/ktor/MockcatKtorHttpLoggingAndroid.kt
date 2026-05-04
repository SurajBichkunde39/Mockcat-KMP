package com.mockcat.logger.ktor

import android.content.Context
import com.mockcat.logger.persistence.ProcessHttpLogStore
import io.ktor.client.HttpClientConfig

fun HttpClientConfig<*>.installMockcatKtorHttpLogging(
    context: Context,
    maxResponseBodyBytes: Long = 256L * 1024L,
) {
    val store = ProcessHttpLogStore.get(context)
    install(MockcatKtorHttpLogging) {
        writer = store
        this.maxResponseBodyBytes = maxResponseBodyBytes
    }
}
