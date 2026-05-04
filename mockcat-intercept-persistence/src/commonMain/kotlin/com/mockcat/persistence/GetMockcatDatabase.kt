package com.mockcat.persistence

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

fun getMockcatDatabase(builder: RoomDatabase.Builder<MockcatDatabase>): MockcatDatabase = builder
    // Building phase: no hand-written migrations; schema changes clear local DB. Add real migrations before release.
    .fallbackToDestructiveMigration(dropAllTables = true)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
