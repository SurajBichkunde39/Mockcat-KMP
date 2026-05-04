package com.mockcat.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mockcat.api.MockType

@Entity(tableName = "mocks")
data class MockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val url: String,
    val label: String = "",
    val httpMethod: String,
    val isEnabled: Boolean = true,
    val mockType: MockType = MockType.STATIC,
    val responseCode: Int? = null,
    val responseBody: String? = null,
    val delayMs: Long? = null,
    val redirectUrl: String? = null,
    val requiredHeaders: Map<String, String>? = null,
    val requiredQueryParams: Map<String, String>? = null,
)
