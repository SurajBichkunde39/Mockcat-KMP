package com.mockcat.logger

import com.mockcat.api.http.LoggedHttpCall
import kotlinx.coroutines.flow.Flow

/**
 * Append path for the HTTP log pipeline. Implementations assign [LoggedHttpCall.id] when the
 * incoming [LoggedHttpCall.id] is 0L.
 */
interface HttpLogWriter {
    suspend fun emit(call: LoggedHttpCall)
}

/**
 * Read path for the logger UI and tooling.
 */
interface HttpLogReader {
    fun observeLogs(): Flow<List<LoggedHttpCall>>
    suspend fun getById(id: Long): LoggedHttpCall?
    suspend fun clear()
}
