package com.mockcat.logger

import com.mockcat.api.http.LoggedHttpCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Ring buffer of the most recent [maxEntries] calls, newest first. For tests and host apps that
 * do not use [com.mockcat.logger.persistence] yet.
 */
class InMemoryHttpLogStore(
    private val maxEntries: Int = 200,
) : HttpLogWriter,
    HttpLogReader {
    private val mutex = Mutex()
    private var nextId = 1L
    private val state = MutableStateFlow<List<LoggedHttpCall>>(emptyList())

    override fun observeLogs(): Flow<List<LoggedHttpCall>> = state.asStateFlow()

    override suspend fun emit(call: LoggedHttpCall) {
        mutex.withLock {
            val withId = if (call.id == 0L) {
                val id = nextId++
                call.copy(id = id)
            } else {
                call
            }
            val current = state.value
            val next = listOf(withId) + current
            val trimmed = if (next.size > maxEntries) next.take(maxEntries) else next
            state.value = trimmed
        }
    }

    override suspend fun getById(id: Long): LoggedHttpCall? = mutex.withLock {
        state.value.find { it.id == id }
    }

    override suspend fun clear() {
        mutex.withLock { state.value = emptyList() }
    }
}
