package com.mockcat.android.okhttp

import android.content.Context
import com.mockcat.api.MockcatStore
import com.mockcat.logger.persistence.RoomHttpLogStore
import com.mockcat.logger.persistence.getHttpLogStoreForAndroid
import com.mockcat.persistence.getMockcatStoreForAndroid

internal object AndroidHttpLogStoreHolder {
    @Volatile
    private var store: RoomHttpLogStore? = null
    private val lock = Any()

    fun get(context: Context): RoomHttpLogStore {
        store?.let { return it }
        val app = context.applicationContext
        return synchronized(lock) {
            if (store == null) {
                store = getHttpLogStoreForAndroid(app)
            }
            checkNotNull(store)
        }
    }
}

internal object AndroidMockcatStoreHolder {
    @Volatile
    private var store: MockcatStore? = null
    private val lock = Any()

    fun get(context: Context): MockcatStore {
        store?.let { return it }
        val app = context.applicationContext
        return synchronized(lock) {
            if (store == null) {
                store = getMockcatStoreForAndroid(app)
            }
            checkNotNull(store)
        }
    }
}
