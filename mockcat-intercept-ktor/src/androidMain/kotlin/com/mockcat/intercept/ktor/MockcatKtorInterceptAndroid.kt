package com.mockcat.intercept.ktor

import android.content.Context
import com.mockcat.persistence.ProcessMockcatStore
import io.ktor.client.HttpClientConfig

fun HttpClientConfig<*>.installMockcatKtorIntercept(
    context: Context,
) {
    install(MockcatKtorIntercept) {
        store = ProcessMockcatStore.get(context)
    }
}
