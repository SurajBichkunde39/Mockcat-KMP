# Quick Start

This guide gets you from zero to intercepting your first request in about five minutes on Android with OkHttp.

---

## 1. Add dependencies

```kotlin title="app/build.gradle.kts"
dependencies {
    debugImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-intercept-ui:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-ui:0.1.0")

    releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
}
```

Apply the Gradle plugin as well if you want to push mocks from files:

```kotlin title="app/build.gradle.kts"
plugins {
    id("com.mockcat.mockcat-gradle") version "0.1.0"
}
```

---

## 2. Wire OkHttp

Add both interceptors when building your `OkHttpClient`. The order matters: logging should come before the intercept so it records all traffic including mocked responses.

```kotlin
import com.mockcat.intercept.okhttp.MockcatIntercept
import com.mockcat.logger.okhttp.MockcatLogging

val mockcat = MockcatIntercept(context)

val client = OkHttpClient.Builder()
    .addInterceptor(MockcatLogging(context))  // log all traffic
    .addInterceptor(mockcat)                  // intercept matching requests
    .build()
    .also { mockcat.bindClient(it) } // (1)!
```

1. `bindClient` is required if you use redirect-type mocks. It gives the interceptor a reference to the client so it can execute the redirected request.

That's the only change to your HTTP client setup.

---

## 3. Create a mock file

Create a `mocks/` directory alongside your `build.gradle.kts` and add a JSON file:

```json title="app/mocks/users.json"
{
  "entries": [
    {
      "url": "https://api.example.com/users",
      "httpMethod": "GET",
      "responseCode": 200,
      "responseBody": [
        { "id": 1, "name": "Alice" },
        { "id": 2, "name": "Bob" }
      ]
    }
  ]
}
```

The `url` must match exactly what your app sends, **without** a query string. Query params are handled separately — see [Mock Files](../guides/mock-files.md).

---

## 4. Push mocks to your device

With a device or emulator connected:

```bash
./gradlew mockcatImport
```

This replaces all existing mocks in the app with what's in your `mocks/` directory. It is atomic — delete-all + insert-all in one transaction.

---

## 5. Verify it works

Make a request from your app to `https://api.example.com/users`. Instead of hitting the real server, you'll get the response from your JSON file.

To inspect what happened, launch the HTTP log viewer from your app:

```kotlin
startActivity(MockcatLoggerUi.createLaunchIntent(context))
```

You'll see the intercepted request listed with a `200` status badge.

---

## 6. Open the mock editor

The built-in UI lets you create, edit, enable/disable, or delete mocks at runtime — without rerunning `mockcatImport`:

```kotlin
startActivity(MockcatUi.createLaunchIntent(context))
```

---

## What happens in release builds

Because you added `mockcat-noop-android` to `releaseImplementation`, the exact same code compiles in production builds — but `MockcatIntercept` becomes a pass-through interceptor, `MockcatLogging` is a no-op, and no database is created. Nothing to clean up.

---

## Next steps

- [Full OkHttp guide](../guides/okhttp.md) — interceptor options, delays, redirects
- [Ktor guide](../guides/ktor.md) — Ktor plugin setup
- [Mock file format](../guides/mock-files.md) — all supported fields, header + query constraints
- [Gradle plugin](../guides/gradle-plugin.md) — multiple devices, custom mock directories
- [HTTP logging](../guides/http-logging.md) — log viewer, clearing logs, custom log store
