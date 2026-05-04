package com.mockcat.persistence

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters

@Database(
    entities = [MockEntity::class],
    version = 2,
    exportSchema = false,
)
@ConstructedBy(MockcatDatabaseConstructor::class)
@TypeConverters(MockTypeConverters::class)
abstract class MockcatDatabase : RoomDatabase() {
    abstract fun mockDao(): MockDao
}

@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS", "ExpectActualClasses")
expect object MockcatDatabaseConstructor : RoomDatabaseConstructor<MockcatDatabase> {
    override fun initialize(): MockcatDatabase
}
