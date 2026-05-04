package com.mockcat.logger.ktor

import com.mockcat.api.http.LoggedHttpBody
import com.mockcat.api.http.LoggedHttpCall
import com.mockcat.logger.HttpLogWriter
import io.ktor.client.call.replaceResponse
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.readRawBytes
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking

class MockcatKtorHttpLoggingConfig {
    var writer: HttpLogWriter? = null
    var maxResponseBodyBytes: Long = 256L * 1024L
}

private val startKey = AttributeKey<Long>("MockcatKtorHttpLogging_t0")
private val bodyKey = AttributeKey<LoggedHttpBody>("MockcatKtorHttpLogging_reqBody")
private val bodyCacheKey = AttributeKey<BodyBytesCache>("MockcatKtorHttpLogging_cache")
private val loggedKey = AttributeKey<Boolean>("MockcatKtorHttpLogging_done")

private class BodyBytesCache {
    var bytes: ByteArray? = null
}

val MockcatKtorHttpLogging = createClientPlugin("MockcatKtorHttpLogging", ::MockcatKtorHttpLoggingConfig) {
    val writer = requireNotNull(pluginConfig.writer) { "MockcatKtorHttpLogging: set writer" }
    val maxBytes = pluginConfig.maxResponseBodyBytes

    onRequest { request: HttpRequestBuilder, content: Any ->
        request.attributes.put(startKey, System.currentTimeMillis())
        val body = content.toRequestBodyForLogAsOutgoing()
        request.attributes.put(bodyKey, body)
    }

    client.plugin(HttpSend.Plugin).intercept { request: HttpRequestBuilder ->
        val t0 = request.attributes.getOrNull(startKey) ?: System.currentTimeMillis()
        val requestBody = request.attributes.getOrNull(bodyKey) ?: LoggedHttpBody.None
        val requestSnapshot = request.toHttpRequestSnapshot(requestBody)
        val call = try {
            execute(request)
        } catch (e: Exception) {
            val t1 = System.currentTimeMillis()
            @Suppress("TooGenericExceptionCaught")
            try {
                val failure =
                    LoggedHttpCall(
                        id = 0L,
                        request = requestSnapshot,
                        response = null,
                        requestTimestampMs = t0,
                        responseTimestampMs = t1,
                        durationMs = t1 - t0,
                        error = e.message ?: e::class.simpleName,
                    )
                writer.emit(failure)
            } catch (_: Exception) {
            }
            throw e
        }
        val t1 = System.currentTimeMillis()
        // replaceResponse content is not a suspend coroutine: read bytes / emit in runBlocking
        // (JVM + Android; revisit if a native iOS Ktor target is added to this module).
        call.replaceResponse {
            val c = this.call
            var cache = c.attributes.getOrNull(bodyCacheKey)
            if (cache == null) {
                cache = BodyBytesCache()
                c.attributes.put(bodyCacheKey, cache)
            }
            val already = cache.bytes
            if (already != null) {
                return@replaceResponse ByteReadChannel(already, 0, already.size)
            }
            return@replaceResponse runBlocking {
                val read = this@replaceResponse.readRawBytes()
                cache.bytes = read
                if (c.attributes.getOrNull(loggedKey) != true) {
                    val logBody = readResponseBytesForLog(
                        read,
                        maxBytes,
                        this@replaceResponse.contentTypeForLog(),
                    )
                    val responseSnapshot = this@replaceResponse.toHttpResponseSnapshot(logBody)
                    val logged =
                        LoggedHttpCall(
                            id = 0L,
                            request = requestSnapshot,
                            response = responseSnapshot,
                            requestTimestampMs = t0,
                            responseTimestampMs = t1,
                            durationMs = t1 - t0,
                            error = null,
                        )
                    writer.emit(logged)
                    c.attributes.put(loggedKey, true)
                }
                ByteReadChannel(read, 0, read.size)
            }
        }
    }
}
