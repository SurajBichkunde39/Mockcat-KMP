# Ktor Integration

Mockcat provides two Ktor plugins:

- `MockcatKtorIntercept` — intercepts requests and returns saved mock responses
- `MockcatKtorHttpLogging` — captures HTTP traffic to the Mockcat log store

Both are standard Ktor client plugins installed with `install(...)`.

---

## Dependencies

```kotlin title="app/build.gradle.kts"
debugImplementation("com.mockcat:mockcat-intercept-ktor:0.1.0")
debugImplementation("com.mockcat:mockcat-logger-ktor:0.1.0")
releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
```

---

## Basic setup

### Android

On Android, use the convenience helpers that automatically wire the process-wide stores:

```kotlin
import com.mockcat.intercept.ktor.installMockcatKtorIntercept
import com.mockcat.logger.ktor.installMockcatKtorHttpLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

val client = HttpClient(OkHttp) {
    installMockcatKtorIntercept(context)
    installMockcatKtorHttpLogging(context)
    // ... other plugins
}
```

### Using a custom store

If you manage your own `MockcatStore` or `HttpLogWriter`:

```kotlin
import com.mockcat.intercept.ktor.MockcatKtorIntercept
import com.mockcat.logger.ktor.MockcatKtorHttpLogging

val client = HttpClient(OkHttp) {
    install(MockcatKtorIntercept) {
        store = myStore  // Required
    }
    install(MockcatKtorHttpLogging) {
        writer = myLogWriter  // Required
    }
}
```

---

## How the intercept plugin works

The plugin hooks into `client.plugin(HttpSend).intercept`:

1. Checks for the `X-Mockcat-Redirected` header. If present, executes the request normally (prevents redirect loops).
2. Builds an `HttpRequestMetadata` from the `HttpRequestBuilder` (URL, method, all headers).
3. Calls `store.resolveWithMatcher(metadata)`.
4. Returns one of:
   - **`PassThrough`** — delegates to `execute(request)`, the real network call runs
   - **`ApplyStatic`** — returns a `MockcatSyntheticCall` with a `MockcatStaticHttpResponse` containing the mock status, headers, and body bytes
   - **`Redirect`** — creates a new `HttpRequestBuilder`, adds the `X-Mockcat-Redirected` marker, executes it
   - **`Error`** — returns a synthetic call with a 598/599 error response

### Synthetic responses

Ktor's `HttpClientCall` is abstract. Mockcat wraps mock responses in:
- `MockcatSyntheticCall` — the call object returned to the plugin
- `MockcatBuiltHttpRequest` — wraps the original `HttpRequestData`
- `MockcatStaticHttpResponse` — provides status, headers, and body as `ByteReadChannel`

`allowDoubleReceive` is set to `true` on synthetic responses so response bodies can be read multiple times.

---

## How the logging plugin works

The logging plugin hooks into `HttpSend` to intercept responses after the engine executes:

1. Records request timestamp and headers at the `onRequest` phase.
2. After execution, reads the full response body bytes.
3. Emits a `LoggedHttpCall` to the `HttpLogWriter`.
4. Returns a cached copy of the body bytes to the caller so the original response stream is not consumed.

---

## Pre-built client helper

If you want OkHttp with both Mockcat plugins pre-installed as a convenience factory:

```kotlin
import com.mockcat.intercept.ktor.MockcatKtor

val client = MockcatKtor.createHttpClient(context) {
    // additional Ktor config here
}
```

This creates a Ktor `HttpClient(OkHttp)` with both `installMockcatKtorIntercept` and `installMockcatKtorHttpLogging` already applied.

---

## Launching the UI

```kotlin
// Mock rules editor
startActivity(MockcatUi.createLaunchIntent(context))

// HTTP log viewer
startActivity(MockcatLoggerUi.createLaunchIntent(context))
```

---

## Release builds

With `releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")`, `MockcatKtorIntercept` and `MockcatKtorHttpLogging` become plugins that install nothing — the client behaves as if they were never added.
