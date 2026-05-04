package com.mockcat.logger.persistence

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun getHttpLogDatabaseBuilder(): androidx.room.RoomDatabase.Builder<HttpLogDatabase> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )!!
    val path = (documentDirectory.path) + "/$HTTP_LOG_DATABASE_NAME"
    return Room.databaseBuilder<HttpLogDatabase>(
        name = path,
    )
}

fun getHttpLogStoreForIos(): RoomHttpLogStore {
    val database = getHttpLogDatabase(getHttpLogDatabaseBuilder())
    return RoomHttpLogStore(database)
}
