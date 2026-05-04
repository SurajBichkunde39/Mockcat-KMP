package com.mockcat.persistence

import android.content.Context
import com.mockcat.api.MockcatStore

/**
 * Process-wide [MockcatStore] for Android (mock rules DB). One instance per app process.
 */
object ProcessMockcatStore {
    @Volatile
    private var store: MockcatStore? = null
    private val lock = Any()

    fun get(context: Context): MockcatStore {
        store?.let { return it }
        val app = context.applicationContext
        synchronized(lock) {
            if (store == null) {
                store = getMockcatStoreForAndroid(app)
            }
            return checkNotNull(store)
        }
    }
}
