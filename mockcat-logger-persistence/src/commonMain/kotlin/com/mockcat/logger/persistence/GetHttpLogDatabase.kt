package com.mockcat.logger.persistence

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun getHttpLogDatabase(builder: RoomDatabase.Builder<HttpLogDatabase>): HttpLogDatabase = builder
    .fallbackToDestructiveMigration(dropAllTables = true)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
