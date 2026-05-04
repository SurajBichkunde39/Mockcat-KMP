package com.mockcat.logger

import kotlinx.atomicfu.atomic

/**
 * Process-wide [HttpLogReader] for the logger UI (separate activity / iOS view controller) so the
 * host app does not pass a reader in. The reader is the same [HttpLogWriter] implementation used
 * for logging in most cases (e.g. [com.mockcat.logger.persistence.RoomHttpLogStore]).
 */
object HttpLogReaderRegistry {
    private val current = atomic<HttpLogReader?>(null)

    /**
     * Sets the reader the first time it is called. Later calls with a different value are ignored
     * to keep a single process-wide log.
     */
    fun install(reader: HttpLogReader) {
        while (true) {
            val c = current.value
            if (c != null) {
                return
            }
            if (current.compareAndSet(null, reader)) {
                return
            }
        }
    }

    fun currentOrNull(): HttpLogReader? = current.value

    fun requireCurrent(): HttpLogReader = currentOrNull()
        ?: error(
            "HttpLogReader is not installed. " +
                "On Android, add com.mockcat.logger.okhttp.MockcatLogging to your OkHttp stack " +
                "(or installMockcatKtorHttpLogging). " +
                "On iOS, call the bootstrap that provides getHttpLogStoreForIos() and registers the reader " +
                "(or call install() with the shared [RoomHttpLogStore]).",
        )

    /**
     * Test-only: clears the reference.
     */
    fun clear() {
        current.value = null
    }
}
