# Mockcat

**Mockcat** is a Kotlin Multiplatform toolkit for HTTP mocking, traffic inspection, and interactive mock rule editing — without touching your production code.

📖 **[Full documentation →](https://surajbichkunde39.github.io/Mockcat-KMP/)**

---

## What it does

- **Intercept** — Return saved responses or redirect requests at runtime. Rules are stored in a local database, editable in a built-in Compose UI.
- **Log** — Capture every HTTP call (headers, body, timing) and inspect it in-app without a proxy.
- **Push mocks over ADB** — The Gradle plugin reads JSON files from your project and loads them into a connected device in one command.

Supports Android (OkHttp, Ktor) and iOS (URLSession).

---

## Quick start

```kotlin title="app/build.gradle.kts"
dependencies {
    debugImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-intercept-ui:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-ui:0.1.0")
    releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
}
```

```kotlin
val mockcat = MockcatIntercept(context)
val client = OkHttpClient.Builder()
    .addInterceptor(MockcatLogging(context))
    .addInterceptor(mockcat)
    .build()
    .also { mockcat.bindClient(it) }
```

```bash
./gradlew mockcatImport   # push mocks from app/mocks/*.json to connected device
```

See the **[Quick Start guide](https://surajbichkunde39.github.io/Mockcat-KMP/getting-started/quick-start/)** for the full walkthrough.

---

## Module layout

| Module | Role |
|--------|------|
| `mockcat-api` | Shared types: mocks, matchers, `MockcatStore` contract (KMP) |
| `mockcat-intercept-persistence` | Room database and `MockcatStore` implementation |
| `mockcat-intercept-ui` | Compose mock rules editor |
| `mockcat-intercept-okhttp` | OkHttp interceptor |
| `mockcat-intercept-ktor` | Ktor client plugin |
| `mockcat-intercept-urlsession` | iOS URLSession / `NSURLProtocol` bridge |
| `mockcat-logger-core` | `HttpLogReader` / `HttpLogWriter` interfaces + registry |
| `mockcat-logger-persistence` | Room-backed HTTP log store |
| `mockcat-logger-okhttp` / `mockcat-logger-ktor` / `mockcat-logger-urlsession` | Platform loggers |
| `mockcat-logger-ui` | Compose HTTP log list + detail viewer |
| `mockcat-noop-android` | No-op Android artifact — same API, zero implementation, empty manifest |
| `mockcat-gradle-plugin` | `mockcatImport` Gradle task for ADB mock push |
| `sample-compose` | Android sample app |
| `sample-server` | Demo Ktor server for the sample app |
| `iosApp/MockcatSample` | iOS SwiftUI sample |

---

## Documentation

Full documentation at **[surajbichkunde39.github.io/Mockcat-KMP](https://surajbichkunde39.github.io/Mockcat-KMP/)**:

- [Installation](https://surajbichkunde39.github.io/Mockcat-KMP/getting-started/installation/)
- [Quick Start](https://surajbichkunde39.github.io/Mockcat-KMP/getting-started/quick-start/)
- [OkHttp guide](https://surajbichkunde39.github.io/Mockcat-KMP/guides/okhttp/)
- [Ktor guide](https://surajbichkunde39.github.io/Mockcat-KMP/guides/ktor/)
- [Mock file format](https://surajbichkunde39.github.io/Mockcat-KMP/guides/mock-files/)
- [Gradle plugin](https://surajbichkunde39.github.io/Mockcat-KMP/guides/gradle-plugin/)
- [Production builds](https://surajbichkunde39.github.io/Mockcat-KMP/guides/production-builds/)
- [Architecture](https://surajbichkunde39.github.io/Mockcat-KMP/concepts/architecture/)
- [Mock matching](https://surajbichkunde39.github.io/Mockcat-KMP/concepts/matching/)

---

## Requirements

- **JDK 17+**
- **Android Gradle Plugin 9.x**, **Kotlin 2.x**, **minSdk 24+**
- **iOS**: Xcode 15+, `iosArm64` / `iosSimulatorArm64`

## Contributing

Prefer small, focused changes. Keep `commonMain` free of platform types in `mockcat-api`. After Kotlin edits, run `./gradlew ktlintCheck detekt`. Open issues or pull requests against the default branch.
