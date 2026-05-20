# Mockcat

**Mockcat** is a Kotlin Multiplatform toolkit for HTTP mocking, traffic inspection, and interactive mock rule editing — without touching your production code.

---

## What it does

Mockcat sits between your HTTP client and the network. When a request matches a rule you defined, it returns a pre-configured response. When nothing matches, the request goes through to the real server — no setup required for that part.

```
Your App  →  MockcatIntercept  →  (matched) → return saved response
                                  (no match) → real network call
```

It has three independent capabilities you can mix and match:

**Intercept** — Return saved responses or redirect requests without changing app code. Rules are stored in a local database and can be toggled on/off at runtime from the built-in UI.

**Log** — Capture every HTTP call (request + response headers, body, timing) in a persistent log you can inspect in-app. Useful for debugging and QA without a proxy.

**Push mocks over ADB** — The Gradle plugin reads JSON files from your project, merges them, and loads them into a connected device in one command. Useful in CI or team workflows.

---

## Platform support

| Feature | Android (OkHttp) | Android (Ktor) | iOS (URLSession) |
|---------|:---:|:---:|:---:|
| Intercept (static + redirect) | ✓ | ✓ | ✓ |
| HTTP Logging | ✓ | ✓ | ✓ |
| Mock editor UI | ✓ | ✓ | – (planned) |
| HTTP log viewer UI | ✓ | ✓ | ✓ |
| Gradle plugin ADB push | ✓ | ✓ | – |

---

## Key design decisions

**No DI framework.** Mockcat doesn't inject anything into your app. You create and wire the interceptors yourself, same as you do with OkHttp's `addInterceptor`.

**No production overhead.** The `mockcat-noop-android` artifact has the exact same public API as the real library — no activities, no database, all methods are no-ops. Swap it in for release builds and your app code changes nothing.

**Mocks directory as source of truth.** When you push mocks via the Gradle plugin, it replaces all existing rules atomically. What's in your `mocks/` folder on disk is exactly what's in the app.

---

## Quick example

```kotlin
// Wire up OkHttp with both intercept + logging
val mockcat = MockcatIntercept(context)
val client = OkHttpClient.Builder()
    .addInterceptor(MockcatLogging(context))
    .addInterceptor(mockcat)
    .build()
    .also { mockcat.bindClient(it) }

// Launch the mock editor from anywhere in your app
startActivity(MockcatUi.createLaunchIntent(context))

// Launch the HTTP log viewer
startActivity(MockcatLoggerUi.createLaunchIntent(context))
```

```json
// mocks/api.json
{
  "entries": [
    {
      "url": "https://api.example.com/users",
      "httpMethod": "GET",
      "responseCode": 200,
      "responseBody": [{ "id": 1, "name": "Alice" }]
    }
  ]
}
```

```bash
# Push mocks to connected device
./gradlew mockcatImport
```

---

[Get started →](getting-started/installation.md){ .md-button .md-button--primary }
[See the quick start →](getting-started/quick-start.md){ .md-button }
