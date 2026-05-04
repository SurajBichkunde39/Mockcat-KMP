package com.mockcat.persistence

import androidx.room.Room
import com.mockcat.api.MockcatStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun getMockcatDatabaseBuilder(): androidx.room.RoomDatabase.Builder<MockcatDatabase> {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )!!
    val path = (documentDirectory.path) + "/$MOCKCAT_DATABASE_NAME"
    return Room.databaseBuilder<MockcatDatabase>(
        name = path,
    )
}

fun getMockcatStoreForIos(): MockcatStore {
    val database = getMockcatDatabase(getMockcatDatabaseBuilder())
    return RoomMockcatStore(database)
}
