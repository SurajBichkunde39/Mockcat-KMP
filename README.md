# Mockcat

**Mockcat** is a Kotlin Multiplatform toolkit for **recording HTTP traffic**, **editing mock rules** in a Compose UI, and **intercepting** client calls so matching requests return saved responses instead of hitting the network. It targets Android (OkHttp, Ktor) and iOS (URLSession), with Room-backed persistence for mocks and logs.

## What you get

- **Intercept** — Static and redirect-style rules keyed by URL (without query) and HTTP method, with optional header and query constraints. Android: OkHttp interceptor and Ktor `HttpSend` plugin. iOS: `NSURLProtocol` integration on a `URLSessionConfiguration` you own.
- **Logging** — Persisted HTTP call history (OkHttp, Ktor, URLSession) and a list/detail experience in Compose.
- **UI** — Multiplatform mock editor and HTTP log screens; Android activities and iOS `UIViewController` entry points ship in library modules.
- **Integration** — A Gradle plugin for workflows that push mocks over ADB.

Library modules intentionally avoid a DI framework; apps wire `MockcatStore` and `HttpClient` / `URLSession` manually.

## Repository layout

| Module | Role |
|--------|------|
| `mockcat-api` | Shared types: mocks, matchers, `MockcatStore` contract |
| `mockcat-intercept-persistence` | Room database and `MockcatStore` implementation |
| `mockcat-intercept-ui` | Compose mock rules editor |
| `mockcat-intercept-okhttp` | OkHttp interceptor |
| `mockcat-intercept-ktor` | Ktor client plugin |
| `mockcat-intercept-urlsession` | iOS URLSession / `NSURLProtocol` bridge |
| `mockcat-logger-core` | `HttpLogReader` registry |
| `mockcat-logger-persistence` | Room-backed HTTP log store |
| `mockcat-logger-okhttp` / `mockcat-logger-ktor` / `mockcat-logger-urlsession` | Platform loggers |
| `mockcat-logger-ui` | Compose HTTP log list |
| `mockcat-noop-android` | No-op Android artifact — same API, zero implementation, empty manifest |
| `mockcat-gradle-plugin` | JVM Gradle plugin (e.g. ADB broadcast import) |
| `sample-compose` | Android sample app |
| `sample-server` | Demo Ktor server for samples |
| `iosApp/MockcatSample` | iOS SwiftUI sample (URLSession + KMP frameworks) |

For module dependencies, iOS framework names, and sample wiring, see **[AGENT.md](AGENT.md)**.

## Integration

Mockcat is a development tool and must not ship in production builds. The library and its no-op counterpart share an identical public API — swap them per build variant and your app code needs no `#if DEBUG` guards anywhere.

### Android

Add the real library to non-production variants and the no-op to production:

```kotlin
// app/build.gradle.kts
dependencies {
    debugImplementation("com.mockcat:mockcat-intercept-okhttp:<version>")
    debugImplementation("com.mockcat:mockcat-intercept-ktor:<version>")
    debugImplementation("com.mockcat:mockcat-intercept-ui:<version>")
    debugImplementation("com.mockcat:mockcat-logger-okhttp:<version>")
    debugImplementation("com.mockcat:mockcat-logger-ktor:<version>")
    debugImplementation("com.mockcat:mockcat-logger-ui:<version>")

    // No-op replaces all of the above in production: same API, empty methods, blank manifest.
    releaseImplementation("com.mockcat:mockcat-noop-android:<version>")
}
```

**With product flavors** — Mockcat works in any non-production variant (e.g. QA release builds). Wire it to whichever configurations are non-production in your setup:

```kotlin
dependencies {
    // QA and dev builds get the real library
    qaDebugImplementation("com.mockcat:mockcat-intercept-okhttp:<version>")
    qaReleaseImplementation("com.mockcat:mockcat-intercept-okhttp:<version>")
    // ... other mockcat modules

    // Only production variants get the no-op
    prodDebugImplementation("com.mockcat:mockcat-noop-android:<version>")
    prodReleaseImplementation("com.mockcat:mockcat-noop-android:<version>")
}
```

**What the no-op guarantees:**
- No `MockcatActivity` or `HttpLogListActivity` registered in the merged manifest
- No Room database created
- No network interception — all requests pass through untouched
- No log capture — `observeLogs()` returns an empty `Flow`
- Zero overhead in production code paths

### iOS

The KMP frameworks are linked as **debug-only** by convention. In `link_kmp.sh`, Gradle builds `linkDebugFramework*` targets, and `project.yml` references the `debugFramework` output paths. For a production archive, remove the Mockcat framework dependencies from the Xcode target or restrict them to the `Debug` configuration in your `project.yml`:

```yaml
# project.yml — restrict to Debug only
dependencies:
  - framework: path/to/MockcatLoggerUI.framework
    embed: true
    configurations: [Debug]
```

A no-op XCFramework for iOS is planned for a future release.

## Gradle plugin — push mocks over ADB

The `mockcat-gradle-plugin` lets you load mock rules from JSON files on the build machine and push them to a connected device or emulator without touching app code. Each run replaces **all** mocks atomically (delete-all + insert-all in one transaction).

### 1. Apply the plugin

```kotlin
// app/build.gradle.kts
plugins {
    id("com.mockcat.mockcat-gradle") version "<version>"
}
```

### 2. Add mock files and run

Put mock JSON files in a `mocks/` directory next to your `build.gradle.kts` and run:

```bash
./gradlew mockcatImport
```

The plugin auto-configures everything:

| Property | Default | Override with |
|----------|---------|---------------|
| `adbExecutable` | `$ANDROID_HOME/platform-tools/adb`, then `adb` on PATH | `adbExecutable.set(file(...))` |
| `applicationId` | Read from AGP `ApplicationExtension.defaultConfig` | `applicationId.set("com.example.app")` |
| `mockFiles` | All `*.json` under `mocks/` | `mockFiles.from(...)` / `mockFiles.setFrom(...)` |
| `deviceSerial` | `$ANDROID_SERIAL` env var | `deviceSerial.set("emulator-5554")` |

The receiver that handles the broadcast is declared in the `mockcat-intercept-persistence` library manifest and merges into your app automatically — no extra wiring required.

### 3. Write mock files

Each file can contain a single entry or a wrapped array. Both formats are accepted.

**Single entry:**
```json
{
  "url": "https://api.example.com/movies",
  "httpMethod": "GET",
  "responseCode": 200,
  "responseBody": [{"id": 1, "title": "Inception"}]
}
```

**Multiple entries in one file:**
```json
{
  "entries": [
    {
      "url": "https://api.example.com/movies",
      "httpMethod": "GET",
      "responseCode": 200,
      "responseBody": [{"id": 1, "title": "Inception"}]
    },
    {
      "url": "https://api.example.com/movies/1",
      "httpMethod": "GET",
      "responseCode": 404,
      "responseBody": {"error": "not found"}
    }
  ]
}
```

Available fields (all optional except `url` and `httpMethod`):

| Field | Type | Description |
|-------|------|-------------|
| `url` | string | Base URL without query string |
| `httpMethod` | string | `GET`, `POST`, etc. |
| `label` | string | Human-readable name shown in the UI |
| `isEnabled` | bool | Default `true`; disabled rules are stored but skipped |
| `mockType` | string | `STATIC` (default) or `REDIRECT` |
| `responseCode` | int | HTTP status code for static responses |
| `responseBody` | any JSON | Response body (object, array, string, …) |
| `delayMs` | long | Artificial delay added before returning the response |
| `redirectUrl` | string | Target URL when `mockType` is `REDIRECT` |
| `requiredHeaders` | object | Header key/value pairs that must be present on the request |
| `requiredQueryParams` | object | Query param key/value pairs that must be present on the request |

### 4. Run

```bash
./gradlew mockcatImport
```

### Multiple connected devices

When only one device is connected the task works with no extra configuration. For multiple devices, set the target serial via the env var ADB already uses:

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew mockcatImport
```

Or configure it permanently in `build.gradle.kts`:

```kotlin
tasks.named<com.mockcat.gradle.MockcatImportTask>("mockcatImport") {
    deviceSerial.set("emulator-5554")
}
```

---

## Requirements

- **JDK** 17+ (align with your Gradle / Android toolchain).
- **Android** — Android Gradle Plugin and compile SDK as defined in `gradle/libs.versions.toml`.
- **iOS** — Xcode; KMP targets in this repo include `iosArm64` and `iosSimulatorArm64` where applicable. The bundled sample builds simulator frameworks via Gradle and links them from Xcode (see `iosApp/MockcatSample/README.md`).

**Toolchain note:** KSP and Kotlin versions must stay aligned (`gradle/libs.versions.toml`). Room on Apple targets must use a Kotlin native ABI compatible with the published Room artifacts.

## Quick start (Gradle)

After cloning, install the shared git hooks (runs ktlint + detekt before every commit):

```bash
./gradlew installGitHooks
```

Then build:

```bash
./gradlew build
```

Run the demo server and Android sample (typical local flow):

```bash
./gradlew :sample-server:run
./gradlew :sample-compose:installDebug
```

Quality checks used in development:

```bash
./gradlew ktlintCheck detekt
```

## Samples

- **`sample-compose`** — Movies list against `sample-server`; demonstrates OkHttp + intercept + logging + mock editor. See **AGENT.md** for `BASE_URL` and emulator networking (`10.0.2.2`, etc.).
- **`iosApp/MockcatSample`** — SwiftUI app using `URLSession` with Mockcat intercept and logger frameworks; see **`iosApp/MockcatSample/README.md`** for Xcode / `xcodegen` and `link_kmp.sh`.

## Documentation

- **[AGENT.md](AGENT.md)** — Maintainer-oriented map of modules, samples, Android/iOS integration notes, and conventions (no DI in libraries, Room migrations, CI expectations).

## Contributing

- Prefer small, focused changes; keep `commonMain` free of platform types in `mockcat-api`.
- After substantive Kotlin edits, run **ktlint** and **detekt** (`./gradlew ktlintCheck detekt`).
- Open issues or pull requests against the default branch; use feature branches for larger work.
