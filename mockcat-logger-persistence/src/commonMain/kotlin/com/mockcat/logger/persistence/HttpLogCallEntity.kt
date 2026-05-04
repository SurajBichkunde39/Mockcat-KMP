package com.mockcat.logger.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "http_log_calls",
    indices = [Index(value = ["capturedAtMs"])],
)
data class HttpLogCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo("requestMethod") val requestMethod: String,
    @ColumnInfo("requestUrl") val requestUrl: String,
    @ColumnInfo("responseCode") val responseCode: Int?,
    @ColumnInfo("capturedAtMs") val capturedAtMs: Long?,
    @ColumnInfo("dataJson") val dataJson: String,
)
