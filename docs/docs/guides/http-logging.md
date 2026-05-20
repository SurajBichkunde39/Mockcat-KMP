# HTTP Logging

Mockcat includes a persistent HTTP traffic logger that captures every request and response — including mocked ones — and presents them in a searchable list/detail UI.

---

## Dependencies

```kotlin title="app/build.gradle.kts"
debugImplementation("com.mockcat:mockcat-logger-okhttp:0.1.0")  // OkHttp
debugImplementation("com.mockcat:mockcat-logger-ktor:0.1.0")    // Ktor
debugImplementation("com.mockcat:mockcat-logger-ui:0.1.0")      // UI viewer
releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
```

---

## Setup

=== "OkHttp"

    ```kotlin
    import com.mockcat.logger.okhttp.MockcatLogging

    val client = OkHttpClient.Builder()
        .addInterceptor(MockcatLogging(context))
        .addInterceptor(MockcatIntercept(context))
        .build()
    ```

=== "Ktor"

    ```kotlin
    import com.mockcat.logger.ktor.installMockcatKtorHttpLogging

    val client = HttpClient(OkHttp) {
        installMockcatKtorHttpLogging(context)
        installMockcatKtorIntercept(context)
    }
    ```

No other configuration is required. Logs are persisted in a Room database and survive process restarts.

---

## Opening the log viewer

```kotlin
import com.mockcat.logger.ui.MockcatLoggerUi

startActivity(MockcatLoggerUi.createLaunchIntent(context))
```

The viewer opens in its own back-stack (`singleTask` with a dedicated `taskAffinity`). Calling `createLaunchIntent` while the viewer is already open brings it to the front rather than creating a new instance.

### iOS

```swift
import MockcatLoggerUI

let logViewController = createHttpLogListViewController()
present(logViewController, animated: true)
```

---

## What gets logged

Each captured call records:

| Field | Description |
|-------|-------------|
| URL | Full request URL including query string |
| Method | GET, POST, etc. |
| Request headers | All request headers |
| Status code | HTTP response status |
| Response headers | All response headers |
| Response body | Up to the capture limit (see below) |
| Duration | Time from request start to response end (ms) |
| Timestamps | Request and response epoch times |
| Error | Non-null if the request failed with an exception |

### Body capture limit

OkHttp uses `response.peekBody()` to read up to **256 KB** of the response body without consuming it. Larger bodies are recorded as `Omitted(reason = "body exceeds limit")`.

To change the limit for OkHttp:

```kotlin
import com.mockcat.logger.okhttp.MockcatHttpLoggingInterceptor

val logger = MockcatHttpLoggingInterceptor(
    writer = MockcatLogging.logReader(context),
    maxResponseBodyBytes = 1 * 1024 * 1024L // 1 MB
)
```

For Ktor, the logging plugin reads the full response body bytes and caches them in memory before re-delivering them to the caller. The captured body size is limited by available memory.

---

## Log viewer features

**List screen:**
- Every HTTP call sorted by time (newest first)
- Status badge colour: green (2xx), orange (3xx), red (4xx/5xx/error)
- Method, host, and path at a glance
- Duration and response body size
- Pull to clear all logs

**Detail screen:**
- Full URL, method, status
- Request headers (expandable)
- Response headers (expandable)
- Request and response body
- Copy as cURL command
- Share via system share sheet

---

## Reading logs programmatically

```kotlin
import com.mockcat.logger.core.HttpLogReaderRegistry

val reader = HttpLogReaderRegistry.requireCurrent()

// Observe as a Flow
reader.observeLogs().collect { calls ->
    // calls: List<LoggedHttpCall>
}

// Get a specific call
val call = reader.getById(callId)

// Clear all logs
reader.clear()
```

### LoggedHttpCall structure

```kotlin
data class LoggedHttpCall(
    val id: Long,
    val request: HttpRequestSnapshot,
    val response: HttpResponseSnapshot?,  // null if request failed
    val requestTimestampMs: Long,
    val responseTimestampMs: Long,
    val durationMs: Long?,
    val error: String?,  // non-null if request threw an exception
)
```

---

## How the store is wired

`MockcatLogging(context)` and `installMockcatKtorHttpLogging(context)` both use `ProcessHttpLogStore.get(context)` — a process-wide singleton backed by a Room database. The log viewer reads from `HttpLogReaderRegistry`, which holds a reference to the same store.

If you need a custom log store (e.g., for testing or a multi-process app):

```kotlin
import com.mockcat.logger.core.InMemoryHttpLogStore
import com.mockcat.logger.core.HttpLogReaderRegistry

val store = InMemoryHttpLogStore()  // ring buffer, 200 entries max
HttpLogReaderRegistry.install(store)

val logger = MockcatHttpLoggingInterceptor(writer = store)
```

---

## iOS bootstrap

Call once at app startup:

```swift
import MockcatLoggerUI

installHttpLogReaderForIos()
```

This initializes the Room-backed log store for iOS, registers it with `HttpLogReaderRegistry`, and shows a floating overlay button that opens the log viewer.
