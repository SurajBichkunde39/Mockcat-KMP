# OkHttp Integration

Mockcat's OkHttp integration consists of two independent interceptors:

- `MockcatIntercept` — resolves requests against stored mock rules, returns a saved response or passes through
- `MockcatLogging` — captures request + response snapshots and writes them to the HTTP log store

Both are standard OkHttp `Interceptor` implementations. You add them to your client builder just like any other interceptor.

---

## Dependencies

```kotlin title="app/build.gradle.kts"
debugImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
debugImplementation("com.mockcat:mockcat-logger-okhttp:0.1.0")
releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
```

---

## Basic setup

```kotlin
import com.mockcat.intercept.okhttp.MockcatIntercept
import com.mockcat.logger.okhttp.MockcatLogging

val mockcatIntercept = MockcatIntercept(context)
val mockcatLogger = MockcatLogging(context)

val client = OkHttpClient.Builder()
    .addInterceptor(mockcatLogger)
    .addInterceptor(mockcatIntercept)
    .build()
    .also { mockcatIntercept.bindClient(it) }
```

### Interceptor order

Place `MockcatLogging` **before** `MockcatIntercept` in the chain. This way, the logger sees both real and mocked traffic — if the intercept returns early with a mock response, the logger still records it.

### `bindClient`

`bindClient(client)` gives the intercept interceptor a reference to the fully built `OkHttpClient`. This is only used when a mock rule has `mockType = REDIRECT` — the interceptor needs a client to make the redirected request. If you don't use redirect mocks, the call is a no-op, but it's safe to always include it.

---

## Using your own store

The convenience constructors `MockcatIntercept(context)` and `MockcatLogging(context)` use the process-wide singleton stores (`ProcessMockcatStore` and `ProcessHttpLogStore`). If you need to use a custom store — for example in a multi-process app or for testing — use the lower-level constructors:

```kotlin
import com.mockcat.intercept.okhttp.MockcatOkHttpInterceptor
import com.mockcat.logger.okhttp.MockcatHttpLoggingInterceptor

val myStore: MockcatStore = ...
val myLogWriter: HttpLogWriter = ...

val interceptor = MockcatOkHttpInterceptor(store = myStore)
val logger = MockcatHttpLoggingInterceptor(writer = myLogWriter)

val client = OkHttpClient.Builder()
    .addInterceptor(logger)
    .addInterceptor(interceptor)
    .build()
    .also { interceptor.setClient(it) }
```

---

## How the interceptor resolves a request

For each request, `MockcatOkHttpInterceptor`:

1. Checks for the `X-Mockcat-Redirected` header. If present, the request passes through immediately (prevents redirect loops).
2. Extracts the base URL (URL without query string), method, and headers into an `HttpRequestMetadata`.
3. Queries the database for enabled mocks matching that base URL and method.
4. Runs the [matching algorithm](../concepts/matching.md) across candidates.
5. Returns one of:
   - **`PassThrough`** — calls `chain.proceed(request)`, the real network request runs
   - **`ApplyStatic`** — builds a synthetic `Response` with the configured status code, headers, and body
   - **`Redirect`** — creates a new call to `redirectUrl`, adds the `X-Mockcat-Redirected` marker, executes it
   - **`Error`** — returns a 598/599 error response with a description

---

## Synthetic responses

Static mock responses include an `X-Mockcat-Source: static-mock` header so you can identify them in the log viewer or in tests.

Protocol-level headers (`Content-Length`, `Transfer-Encoding`, `Connection`, `Keep-Alive`) are excluded from synthetic responses — OkHttp manages these itself.

### Delays

If a mock rule has `delayMs` set, the interceptor sleeps for that duration before returning the response. This is useful for testing loading states and timeouts.

```json
{
  "url": "https://api.example.com/slow",
  "httpMethod": "GET",
  "responseCode": 200,
  "responseBody": { "result": "ok" },
  "delayMs": 2000
}
```

---

## How the logger works

`MockcatHttpLoggingInterceptor`:

1. Records request start time.
2. Calls `chain.proceed(request)` and records end time.
3. Peeks the response body using OkHttp's `response.peekBody(maxBytes)` — this reads up to `maxResponseBodyBytes` (default **256 KB**) without consuming the body.
4. Emits a `LoggedHttpCall` to the `HttpLogWriter`.
5. Returns the original response unmodified.

### Changing the body capture limit

```kotlin
MockcatHttpLoggingInterceptor(
    writer = myWriter,
    maxResponseBodyBytes = 512 * 1024L, // 512 KB
)
```

---

## Launching the UI

```kotlin
// Mock rules editor
startActivity(MockcatUi.createLaunchIntent(context))

// HTTP log viewer
startActivity(MockcatLoggerUi.createLaunchIntent(context))
```

Both activities use `singleTask` and a dedicated `taskAffinity`, so they open in their own back-stack. Calling `createLaunchIntent` again while one is already open brings it to the front.

---

## Release builds

With `releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")`, both `MockcatIntercept` and `MockcatLogging` become interceptors that immediately call `chain.proceed(request)`. There is no database, no log store, and no manifest entries in release.
