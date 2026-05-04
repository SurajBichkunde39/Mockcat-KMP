package com.mockcat.persistence

import android.content.Context
import androidx.room.Room
import com.mockcat.api.MockcatStore

fun getMockcatDatabaseBuilder(context: Context): androidx.room.RoomDatabase.Builder<MockcatDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(MOCKCAT_DATABASE_NAME)
    return Room.databaseBuilder<MockcatDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

fun getMockcatStoreForAndroid(context: Context): MockcatStore {
    val database = getMockcatDatabase(getMockcatDatabaseBuilder(context))
    return RoomMockcatStore(database)
}
