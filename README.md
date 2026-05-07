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
| `mockcat-gradle-plugin` | JVM Gradle plugin (e.g. ADB broadcast import) |
| `sample-compose` | Android sample app |
| `sample-server` | Demo Ktor server for samples |
| `iosApp/MockcatSample` | iOS SwiftUI sample (URLSession + KMP frameworks) |

For module dependencies, iOS framework names, and sample wiring, see **[AGENT.md](AGENT.md)**.

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
