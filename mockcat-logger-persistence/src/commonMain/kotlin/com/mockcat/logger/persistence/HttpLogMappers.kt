package com.mockcat.logger.persistence

import com.mockcat.api.http.LoggedHttpCall
import kotlinx.serialization.json.Json

internal fun LoggedHttpCall.toNewEntity(json: Json): HttpLogCallEntity = HttpLogCallEntity(
    id = 0L,
    requestMethod = request.method,
    requestUrl = request.url,
    responseCode = response?.statusCode,
    capturedAtMs = requestTimestampMs ?: responseTimestampMs,
    dataJson = json.encodeToString(LoggedHttpCall.serializer(), copy(id = 0L)),
)

internal fun HttpLogCallEntity.toCall(json: Json): LoggedHttpCall = json.decodeFromString(LoggedHttpCall.serializer(), dataJson).copy(id = id)
