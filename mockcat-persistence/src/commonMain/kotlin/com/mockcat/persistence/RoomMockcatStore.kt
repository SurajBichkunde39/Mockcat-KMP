package com.mockcat.persistence

import com.mockcat.api.MockEntry
import com.mockcat.api.MockFileEntry
import com.mockcat.api.MockcatStore
import com.mockcat.api.MultipleMockEntries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

private val toFileEntryJson =
    Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

class RoomMockcatStore(
    private val database: MockcatDatabase,
) : MockcatStore {
    private val dao: MockDao = database.mockDao()
    private val importJson =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    private val exportJson = Json { encodeDefaults = true }

    override fun observeAllMocks(): Flow<List<MockEntry>> = dao.getAllMocks().map { list -> list.map { it.toEntry() } }

    override suspend fun getAllMocksOnce(): List<MockEntry> = observeAllMocks().first()

    override suspend fun insertOrUpdate(entry: MockEntry) {
        dao.insertOrUpdate(entry.toEntity())
    }

    override suspend fun delete(entry: MockEntry) {
        if (entry.id == 0L) {
            return
        }
        dao.delete(entry.toEntity())
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun findMatchingMockCandidates(
        urlWithoutQuery: String,
        httpMethod: String,
    ): List<MockEntry> = dao.findMatchingMockCandidates(urlWithoutQuery, httpMethod).map { it.toEntry() }

    override suspend fun importFromJsonReplaceAll(json: String): Int {
        val fileEntries = importJson.decodeFromString<MultipleMockEntries>(json).entries
        val rows = fileEntries.map { it.toEntry().toEntity() }
        dao.importReplace(rows)
        return fileEntries.size
    }

    override suspend fun exportAllToJson(): String = exportJson.encodeToString(
        MultipleMockEntries(
            entries = getAllMocksOnce().map { e: MockEntry -> e.toFileEntry() },
        ),
    )
}

private fun MockEntry.toFileEntry(): MockFileEntry {
    val s = responseBody
    val body: JsonElement? =
        when (s) {
            null -> null
            else ->
                runCatching { toFileEntryJson.parseToJsonElement(s) }
                    .getOrElse { JsonPrimitive(s) }
        }
    return MockFileEntry(
        url = url,
        label = label,
        httpMethod = httpMethod,
        isEnabled = isEnabled,
        mockType = mockType,
        responseCode = responseCode,
        responseBody = body,
        delayMs = delayMs,
        redirectUrl = redirectUrl,
        requiredHeaders = requiredHeaders,
        requiredQueryParams = requiredQueryParams,
    )
}
