package com.mockcat.intercept.okhttp

import com.mockcat.api.MockcatHeaders
import com.mockcat.api.MockcatResult
import com.mockcat.api.MockcatStore
import com.mockcat.api.resolveWithMatcher
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit

class MockcatOkHttpInterceptor(
    private val store: MockcatStore,
) : Interceptor {
    @Volatile
    private var client: OkHttpClient? = null

    /**
     * Breaks the OkHttp/redirect cycle; set after the client is built, like the in-app original.
     */
    fun setClient(okHttpClient: OkHttpClient) {
        this.client = okHttpClient
    }

    private fun isRedirectInFlight(request: Request): Boolean = request.header(MockcatHeaders.REDIRECT_MARKER) != null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (isRedirectInFlight(request)) {
            return chain.proceed(request)
        }
        val result =
            runBlocking {
                val meta = request.toRequestMetadata()
                store.resolveWithMatcher(meta)
            }
        return when (result) {
            is MockcatResult.PassThrough -> chain.proceed(request)
            is MockcatResult.ApplyStatic -> buildStaticResponse(request, result)
            is MockcatResult.Redirect -> {
                if (result.targetUrl.isBlank() || client == null) {
                    return chain.proceed(request)
                }
                newCallResponse(request, result)
            }
            is MockcatResult.Error -> {
                Response.Builder()
                    .code(result.statusCode)
                    .message(result.message)
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body("".toResponseBody("application/json".toMediaTypeOrNull()))
                    .addHeader(MockcatHeaders.SOURCE_NAME, MockcatHeaders.SOURCE_VALUE_ERROR)
                    .build()
            }
        }
    }

    private fun buildStaticResponse(
        request: Request,
        s: MockcatResult.ApplyStatic,
    ): Response {
        if (s.delayMs > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(s.delayMs)
            } catch (_: InterruptedException) {
            }
        }
        val contentType = s.contentType
        return Response.Builder()
            .code(s.statusCode)
            .message(plainMessage(s.statusCode))
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(s.body.toResponseBody((contentType.ifBlank { "application/json" }).toMediaTypeOrNull()))
            .addHeader(MockcatHeaders.SOURCE_NAME, MockcatHeaders.SOURCE_VALUE_STATIC)
            .build()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun newCallResponse(
        request: Request,
        redirect: MockcatResult.Redirect,
    ): Response {
        val c = checkNotNull(client) { "Call setClient(OkHttpClient) before using redirects" }
        val newReq =
            request.newBuilder()
                .url(redirect.targetUrl)
                .addHeader(MockcatHeaders.REDIRECT_MARKER, "true")
                .build()
        return try {
            c.newCall(newReq).execute().let { resp ->
                resp.newBuilder()
                    .request(request)
                    .addHeader(MockcatHeaders.SOURCE_NAME, MockcatHeaders.SOURCE_VALUE_REDIRECT)
                    .build()
            }
        } catch (e: Exception) {
            Response.Builder()
                .code(599)
                .message("Mockcat Redirect Failed: ${e.message}")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body("".toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader(MockcatHeaders.SOURCE_NAME, MockcatHeaders.SOURCE_VALUE_ERROR)
                .build()
        }
    }
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
