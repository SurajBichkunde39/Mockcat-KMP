package com.mockcat.logger.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HttpLogCallDao {
    @Query("SELECT * FROM http_log_calls ORDER BY id DESC")
    fun observeAll(): Flow<List<HttpLogCallEntity>>

    @Query("SELECT * FROM http_log_calls WHERE id = :id")
    suspend fun getById(id: Long): HttpLogCallEntity?

    @Query("DELETE FROM http_log_calls")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(row: HttpLogCallEntity)
}
