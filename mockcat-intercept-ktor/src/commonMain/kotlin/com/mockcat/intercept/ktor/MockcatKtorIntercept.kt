package com.mockcat.intercept.ktor

import com.mockcat.api.HttpRequestMetadata
import com.mockcat.api.MockcatHeaders
import com.mockcat.api.MockcatResult
import com.mockcat.api.MockcatStore
import com.mockcat.api.resolveWithMatcher
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.Sender
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.takeFrom
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import io.ktor.http.takeFrom as takeUrlFrom

class MockcatKtorInterceptConfig {
    var store: MockcatStore? = null
}

/**
 * Engine-agnostic Ktor client plugin: resolves mocks via [MockcatStore] before the request hits
 * the engine. [MockcatResult.Redirect] is handled by an inner [execute] with [MockcatHeaders.REDIRECT_MARKER]
 * (same idea as OkHttp); static and error responses are synthesized without network I/O.
 */
val MockcatKtorIntercept =
    createClientPlugin("MockcatKtorIntercept", ::MockcatKtorInterceptConfig) {
        val store = requireNotNull(pluginConfig.store) { "MockcatKtorIntercept: set store" }
        client.plugin(HttpSend).intercept { request: HttpRequestBuilder ->
            if (request.headers.getAll(MockcatHeaders.REDIRECT_MARKER)?.isNotEmpty() == true) {
                return@intercept execute(request)
            }
            val result =
                runBlocking {
                    store.resolveWithMatcher(request.toMockcatHttpRequestMetadata())
                }
            when (result) {
                is MockcatResult.PassThrough -> execute(request)
                is MockcatResult.ApplyStatic -> staticClientCall(client, request, result)
                is MockcatResult.Redirect -> redirectExecute(request, result)
                is MockcatResult.Error -> errorClientCall(client, request, result)
            }
        }
    }

internal fun HttpRequestBuilder.toMockcatHttpRequestMetadata(): HttpRequestMetadata {
    val pairs = mutableListOf<Pair<String, String>>()
    val built = headers.build()
    for (name in built.names()) {
        val values = built.getAll(name) ?: continue
        for (v in values) {
            pairs.add(name to v)
        }
    }
    return HttpRequestMetadata(
        url = url.buildString(),
        method = method.value,
        headers = pairs,
    )
}

private fun staticClientCall(
    client: HttpClient,
    request: HttpRequestBuilder,
    s: MockcatResult.ApplyStatic,
): HttpClientCall {
    if (s.delayMs > 0) {
        try {
            TimeUnit.MILLISECONDS.sleep(s.delayMs)
        } catch (_: InterruptedException) {
        }
    }
    val reqData = request.build()
    val hb = HeadersBuilder()
    hb.append(HttpHeaders.ContentType, s.contentType)
    for (h in s.responseHeaders) {
        if (shouldEmitSyntheticResponseHeader(h.name)) {
            hb.append(h.name, h.value)
        }
    }
    hb.append(MockcatHeaders.SOURCE_NAME, MockcatHeaders.SOURCE_VALUE_STATIC)
    val bodyBytes = s.body.encodeToByteArray()
    return MockcatSyntheticCall(
        client = client,
        requestData = reqData,
        status = HttpStatusCode(s.statusCode, plainMessage(s.statusCode)),
        responseHeaders = hb.build(),
        bodyBytes = bodyBytes,
    )
}

private fun errorClientCall(
    client: HttpClient,
    request: HttpRequestBuilder,
    e: MockcatResult.Error,
): HttpClientCall {
    val reqData = request.build()
    val hb = HeadersBuilder()
    hb.append(HttpHeaders.ContentType, "application/json")
    hb.append(MockcatHeaders.SOURCE_NAME, MockcatHeaders.SOURCE_VALUE_ERROR)
    return MockcatSyntheticCall(
        client = client,
        requestData = reqData,
        status = HttpStatusCode(e.statusCode, e.message),
        responseHeaders = hb.build(),
        bodyBytes = ByteArray(0),
    )
}

private suspend fun Sender.redirectExecute(
    request: HttpRequestBuilder,
    redirect: MockcatResult.Redirect,
): HttpClientCall {
    if (redirect.targetUrl.isBlank()) {
        return execute(request)
    }
    val child = HttpRequestBuilder()
    child.takeFrom(request)
    child.url.takeUrlFrom(redirect.targetUrl)
    child.headers.append(MockcatHeaders.REDIRECT_MARKER, "true")
    return execute(child)
}

/**
 * [HttpClientCall] without Ktor's internal `(client, HttpRequestData, HttpResponseData)` constructor:
 * uses public [HttpClientCall] primary ctor and assigns [request]/[response] like [io.ktor.client.call.DelegatedCall].
 */
private class MockcatSyntheticCall(
    client: HttpClient,
    requestData: HttpRequestData,
    status: HttpStatusCode,
    responseHeaders: Headers,
    bodyBytes: ByteArray,
) : HttpClientCall(client) {
    init {
        request = MockcatBuiltHttpRequest(this, requestData)
        response =
            MockcatStaticHttpResponse(
                call = this,
                statusCode = status,
                ktorHeaders = responseHeaders,
                bodyBytes = bodyBytes,
                ctx = requestData.executionContext,
            )
    }

    override val allowDoubleReceive: Boolean = true
}

private class MockcatBuiltHttpRequest(
    override val call: HttpClientCall,
    private val data: HttpRequestData,
) : HttpRequest {
    override val coroutineContext: CoroutineContext get() = data.executionContext

    override val method get() = data.method

    override val url get() = data.url

    override val content get() = data.body

    override val headers get() = data.headers

    override val attributes get() = data.attributes
}

@OptIn(InternalAPI::class)
private class MockcatStaticHttpResponse(
    override val call: HttpClientCall,
    private val statusCode: HttpStatusCode,
    private val ktorHeaders: Headers,
    private val bodyBytes: ByteArray,
    private val ctx: CoroutineContext,
) : HttpResponse() {
    override val status: HttpStatusCode get() = statusCode

    override val version: HttpProtocolVersion = HttpProtocolVersion.HTTP_1_1

    override val requestTime: GMTDate = GMTDate()

    override val responseTime: GMTDate = GMTDate()

    override val headers: Headers get() = ktorHeaders

    override val coroutineContext: CoroutineContext get() = ctx

    override val rawContent: ByteReadChannel get() = ByteReadChannel(bodyBytes)
}

private fun shouldEmitSyntheticResponseHeader(name: String): Boolean = when (name.lowercase()) {
    "content-length",
    "transfer-encoding",
    "connection",
    "keep-alive",
    "upgrade",
    "proxy-connection",
    -> false
    else -> true
}

private fun plainMessage(statusCode: Int): String = when (statusCode) {
    200 -> "OK"
    201 -> "Created"
    204 -> "No Content"
    400 -> "Bad Request"
    401 -> "Unauthorized"
    403 -> "Forbidden"
    404 -> "Not Found"
    500 -> "Internal Server Error"
    else -> "OK"
}
