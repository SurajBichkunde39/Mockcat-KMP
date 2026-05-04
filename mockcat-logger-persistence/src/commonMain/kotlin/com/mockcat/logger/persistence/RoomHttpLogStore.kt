package com.mockcat.logger.persistence

import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogReader
import com.mockcat.logger.HttpLogWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RoomHttpLogStore(
    private val database: HttpLogDatabase,
) : HttpLogWriter,
    HttpLogReader {
    private val dao: HttpLogCallDao = database.httpLogCallDao()
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override suspend fun emit(call: LoggedHttpCall) {
        val row = call.toNewEntity(json)
        dao.insert(row)
    }

    override fun observeLogs(): Flow<List<LoggedHttpCall>> = dao.observeAll().map { list -> list.map { it.toCall(json) } }

    override suspend fun getById(id: Long): LoggedHttpCall? = dao.getById(id)?.toCall(json)

    override suspend fun clear() {
        dao.deleteAll()
    }
}
