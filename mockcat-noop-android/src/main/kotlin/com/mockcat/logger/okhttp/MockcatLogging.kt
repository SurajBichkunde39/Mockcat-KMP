package com.mockcat.logger.okhttp

import android.content.Context
import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import okhttp3.Interceptor
import okhttp3.Response

class MockcatLogging(
    @Suppress("UnusedPrivateProperty")
    private val context: Context,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())

    companion object {
        fun logReader(
            @Suppress("UnusedPrivateParameter")
            context: Context,
        ): HttpLogReader = NoOpHttpLogReader
    }
}

private object NoOpHttpLogReader : HttpLogReader {
    override fun observeLogs(): Flow<List<LoggedHttpCall>> = emptyFlow()
    override suspend fun getById(id: Long): LoggedHttpCall? = null
    override suspend fun clear() = Unit
}
