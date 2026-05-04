package com.mockcat.logger.persistence

import android.content.Context
import androidx.room.Room

fun getHttpLogDatabaseBuilder(context: Context): androidx.room.RoomDatabase.Builder<HttpLogDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(HTTP_LOG_DATABASE_NAME)
    return Room.databaseBuilder<HttpLogDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

fun getHttpLogStoreForAndroid(context: Context): RoomHttpLogStore {
    val database = getHttpLogDatabase(getHttpLogDatabaseBuilder(context))
    return RoomHttpLogStore(database)
}
