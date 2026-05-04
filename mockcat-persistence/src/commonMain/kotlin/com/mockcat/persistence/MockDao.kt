package com.mockcat.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(mock: MockEntity)

    @Delete
    suspend fun delete(mock: MockEntity)

    @Query("DELETE FROM mocks")
    suspend fun deleteAll()

    @Transaction
    suspend fun importReplace(entities: List<MockEntity>) {
        deleteAll()
        for (e in entities) {
            insertOrUpdate(e)
        }
    }

    @Query("SELECT * FROM mocks ORDER BY url ASC")
    fun getAllMocks(): Flow<List<MockEntity>>

    @Query("SELECT * FROM mocks WHERE isEnabled = 1 AND url = :url AND httpMethod = :method")
    suspend fun findMatchingMockCandidates(
        url: String,
        method: String,
    ): List<MockEntity>
}
