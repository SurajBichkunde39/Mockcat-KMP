package com.mockcat.logger.persistence

import android.content.Context
import com.mockcat.logger.HttpLogReaderRegistry

/**
 * Process-wide [RoomHttpLogStore] for Android: one instance per app process, shared by OkHttp and
 * Ktor logging. Installs [HttpLogReaderRegistry] the first time the store is created.
 */
object ProcessHttpLogStore {
    @Volatile
    private var store: RoomHttpLogStore? = null
    private val lock = Any()

    fun get(context: Context): RoomHttpLogStore {
        store?.let { return it }
        val app = context.applicationContext
        synchronized(lock) {
            if (store == null) {
                val created = getHttpLogStoreForAndroid(app)
                HttpLogReaderRegistry.install(created)
                store = created
            }
            return checkNotNull(store)
        }
    }
}
