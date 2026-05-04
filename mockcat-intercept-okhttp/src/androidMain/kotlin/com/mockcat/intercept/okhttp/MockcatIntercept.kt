package com.mockcat.intercept.okhttp

import android.content.Context
import com.mockcat.persistence.ProcessMockcatStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class MockcatIntercept private constructor(
    private val mock: MockcatOkHttpInterceptor,
) : Interceptor by mock {
    constructor(
        context: Context,
    ) : this(
        mock = MockcatOkHttpInterceptor(store = ProcessMockcatStore.get(context)),
    )

    fun bindClient(client: OkHttpClient) {
        mock.setClient(client)
    }
}
