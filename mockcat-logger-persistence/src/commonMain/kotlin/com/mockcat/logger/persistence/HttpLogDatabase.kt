package com.mockcat.logger.persistence

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [HttpLogCallEntity::class],
    version = 1,
    exportSchema = false,
)
@ConstructedBy(HttpLogDatabaseConstructor::class)
abstract class HttpLogDatabase : RoomDatabase() {
    abstract fun httpLogCallDao(): HttpLogCallDao
}

@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS", "ExpectActualClasses")
expect object HttpLogDatabaseConstructor : RoomDatabaseConstructor<HttpLogDatabase> {
    override fun initialize(): HttpLogDatabase
}
