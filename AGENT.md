# Mockcat (agent context)

**Do not `git commit` in this repository unless the user explicitly asks to commit** — they prefer to review diffs first and commit when aligned.

**Mockcat** is a Kotlin Multiplatform monorepo for HTTP mocking, traffic logging, shared API and storage, platform interceptors, optional UI, and tooling.

## Layout

| Module | Role |
|--------|------|
| `mockcat-api` | **Shared** HTTP contract: `HttpRequestMetadata`, mock types (`MockEntry`, `MockcatResult`, `MockcatStore`, `MockMatcher`), and **logging** DTOs under `com.mockcat.api.http` (`HttpRequestSnapshot`, `HttpResponseSnapshot`, `LoggedHttpCall`, etc.) |
| `mockcat-persistence` | Room (KMP) + `RoomMockcatStore` for **mock rules only**; bundled SQLite, import/export JSON |
| `mockcat-logger` | `HttpLogWriter` / `HttpLogReader`, `InMemoryHttpLogStore` (ring buffer) — depends on `mockcat-api` only |
| `mockcat-logger-persistence` | **Separate** Room DB for HTTP call logs only (`http_log_db` file); `RoomHttpLogStore`; **does not** depend on `mockcat-persistence` |
| `mockcat-logger-okhttp` | Android: `MockcatHttpLoggingInterceptor` (read-only log + forward); maps OkHttp to `com.mockcat.api.http` |
| `mockcat-okhttp-android` | Android: `addInterceptor(MockcatLogging(context))`, `MockcatIntercept(context)` + `bindClient(OkHttpClient)` after `build()`; `MockcatLogging.logReader` for the log list — process-singleton `RoomHttpLogStore` and `MockcatStore` (same as `getHttpLogStoreForAndroid` / `getMockcatStoreForAndroid`) |
| `mockcat-logger-ui` | Compose Multiplatform: `HttpLogListScreen` (traffic list); separate from the mock **editor** in `mockcat-ui` |
| `mockcat-ui` | Compose Multiplatform UI + `MockcatViewModel` for **mock rules** (manual wiring, **no DI framework in libraries**) |
| `mockcat-intercept-okhttp` | `MockcatOkHttpInterceptor` |
| `mockcat-intercept-ktor` | `MockcatKtor.createHttpClient` (OkHttp engine) |
| `mockcat-intercept-urlsession` | iOS: `NSURLRequest` → `HttpRequestMetadata`, `runMockcatUrlSessionResolve` for `URLProtocol` / Swift |
| `mockcat-integration-chucker` | Android: Chucker share text parser for import |
| `mockcat-gradle-plugin` | JVM plugin: ADB + broadcast import task (consumers register/configure) |
| `sample-compose` | Android sample: movies list/detail, OkHttp + `MockcatOkHttpInterceptor`, Chucker, Ktor demo server |
| `sample-server` | JVM Ktor app: serves `GET /api/movies` and `GET /api/movies/{imdbId}` from `films.json` (port 8080) |
| `iosApp/MockcatSample` | Native SwiftUI sources (Xcode project you create; link KMP frameworks) |

## Conventions

- **Tooling** (see `gradle/libs.versions.toml`): **Gradle 9.3+**, **AGP 9.1.1**, **Kotlin 2.2.21**, **KSP 2.3.7** (KSP 2.3+ is required for AGP 9 + the Android KMP library plugin; see [google/ksp#2476](https://github.com/google/ksp/issues/2476)). **Room 2.8.4** with the Room Gradle plugin in `mockcat-persistence` and `mockcat-logger-persistence` (see below). **Compose Multiplatform** plugin `1.10.3` with **Material3** on its own line (`composeMaterial3`).

- **Android KMP library plugin** (most modules): `com.android.kotlin.multiplatform.library` with a `kotlin { android { namespace; compileSdk; minSdk; compilerOptions { jvmTarget } } }` block. There is **no** top-level `android { }` in those modules. **Android main** compile task name is e.g. `:mockcat-api:compileAndroidMain` (single-variant plugin; not `compileDebugKotlinAndroid`).

- **Room (KMP) `com.android.library` exception:** `mockcat-persistence` and `mockcat-logger-persistence` use `com.android.library` + `androidTarget { publishAllLibraryVariants() }` and the **`androidx.room` Gradle plugin** because the Room plugin is not yet compatible with `com.android.kotlin.multiplatform.library` (class cast on `KotlinMultiplatformAndroidCompilationImpl`). KSP for Room uses `kspAndroid` and `kspKotlinIos*`. Revisit when AndroidX Room supports the new Android KMP target.

- **`gradle.properties`**
  - `android.builtInKotlin=false` — required for `sample-compose` (KMP + `com.android.application`); otherwise the `kotlin` extension is registered twice (see [issuetracker 438678642](https://issuetracker.google.com/issues/438678642)).
  - `android.newDsl=false` — some tooling (Compose MPP, detekt) still use the legacy `Project.android` accessor for the app and `mockcat-integration-chucker`.

- **Quality**: `./gradlew ktlintCheck detekt` (and `ktlintFormat` as needed). Generated output is excluded from ktlint via a root `subprojects` ktlint `filter` with a `FileTreeElement` `exclude` that omits files whose path is under a `build/` directory. Root `.editorconfig` sets `ktlint_function_naming_ignore_when_annotated_with=Composable,Preview` for Compose.

- **Libraries** stay free of Koin/Hilt/Dagger; apps/samples wire dependencies explicitly.

- **Publishing (Plan D)**: KMP + `maven-publish` is configured on several modules. For Android, prefer an explicit `publishLibraryVariants` / publication set per module when you add an internal **Artifactory** or **Maven** repository—use a **local-only** `gradle.properties` (not committed) for `mavenUser` / `mavenPassword` / repository URL. Example (consumer `build.gradle.kts` once you add `maven { url = uri("https://your-artifactory/...") }`):

  ```kotlin
  publishing { repositories { maven { name = "internal"; url = uri(providers.gradleProperty("mockcatRepoUrl").get()) } } }
  ```

## Common tasks

- Android debug: `./gradlew :sample-compose:assembleDebug`
- iOS KSP (Room): `./gradlew :mockcat-persistence:kspKotlinIosSimulatorArm64` and/or `:mockcat-logger-persistence:kspKotlinIosSimulatorArm64` (or `kspKotlinIosArm64`)
- Logger stack compile: `:mockcat-logger:compileAndroidMain`, `:mockcat-logger-persistence:compileDebugKotlinAndroid`, `:mockcat-logger-okhttp:compileAndroidMain`, `:mockcat-okhttp-android:compileAndroidMain`, `:mockcat-logger-ui:compileAndroidMain`
- iOS **frameworks** for the Swift app (simulator arm64, debug):
  - `./gradlew :mockcat-api:linkDebugFrameworkIosSimulatorArm64`
  - `./gradlew :mockcat-persistence:linkDebugFrameworkIosSimulatorArm64`
  - `./gradlew :mockcat-intercept-urlsession:linkDebugFrameworkIosSimulatorArm64`
- Frameworks are emitted under each module’s `build/` tree (see Gradle task output for exact paths). In Xcode, **Embed & Sign** those frameworks, then import in Swift, e.g. `MockcatApi`, `MockcatPersistence`, `MockcatInterceptUrlsession`. The SQLite DB for Room on iOS is under the app’s documents directory (see `DatabaseBuilder.ios.kt`).

## iOS sample (Swift)

- **Sources:** `iosApp/MockcatSample/`. There is no checked-in `.xcodeproj`—create an iOS app target in Xcode, add these Swift files, and link the three frameworks from Gradle.
- **Kotlin:** `getMockcatStoreForIos(): MockcatStore` in `mockcat-persistence`, `RunMockcatUrlSessionResolve` / `toHttpRequestMetadata` in `mockcat-intercept-urlsession` for a Swift `URLProtocol` implementation. Put the full URL (including `?query`) in `HttpRequestMetadata.url` so `baseUrl` / `queryParameters` match OkHttp.
- **URLSession:** set `URLSessionConfiguration.protocolClasses` to your `URLProtocol` **before** creating a `URLSession` that should see mocks.

## Android `sample-compose`

- **Ktor server (host):** `./gradlew :sample-server:run` — listens on `http://127.0.0.1:8080`. The emulator uses `http://10.0.2.2:8080` (see `MovieConfig.BASE_URL` in `com.mockcat.sample.data`); for a physical device, use your machine’s LAN IP or `adb reverse tcp:8080 tcp:8080` and `http://127.0.0.1:8080`.
- **App (MVVM):** `OkHttpClientFactory` uses Chucker + [`MockcatLogging(context)`](mockcat-okhttp-android) + [`MockcatIntercept(context)`](mockcat-okhttp-android) and `bindClient` for redirects. Process-wide stores match `getHttpLogStoreForAndroid` / `getMockcatStoreForAndroid`. `MockcatLogging.logReader(app)` supplies [HttpLogReader](mockcat-logger) to `MoviesViewModel` for the “HTTP log” screen ([HttpLogListScreen](mockcat-logger-ui)). The sample depends on `mockcat-okhttp-android` and `mockcat-logger-ui`. **Next step** for mocks UI: add `mockcat-ui` and `MockcatUi.createLaunchIntent` when desired. Hand-written Room migrations are deferred (destructive fallback). Packages: `data` (config, DTOs, `OkHttpClientFactory`, repository), `ui.movies` (ViewModel, screen), `ui.theme`.

## `mockcat-ui` (Android) — mock **rules** editor

- `MockcatUi.createLaunchIntent(context, newTaskOrDocument = true)` — public entry to `MockcatActivity` (declared in the library manifest, `android:exported="false"`; only your app’s package can start it unless you add a manifest `tools:node` / proxy activity).
- Optional: `MockcatUi.createLaunchPendingIntent` for notifications.

## `mockcat-logger-ui` (Compose MPP) — traffic **log** list

- `HttpLogListScreen` — list of `LoggedHttpCall` (method, path, status or error). Host app supplies `List<LoggedHttpCall>` (e.g. from `InMemoryHttpLogStore` or `RoomHttpLogStore` via `observeLogs()`). Not the same module as the mock editor above.

## Gradle plugin (`com.mockcat.mockcat-gradle`)

Apply in a consumer:

```kotlin
plugins { id("com.mockcat.mockcat-gradle") version "0.1.0-SNAPSHOT" } // or includeBuild

tasks.named<com.mockcat.gradle.MockcatImportTask>("mockcatImport") {
  adbExecutable.set(file("${System.getenv("ANDROID_HOME")}/platform-tools/adb")) // or omit if ANDROID_HOME is set
  applicationId.set("com.mockcat.sample")
  mockFiles.from("mocks/") // or list files
  broadcastAction = "com.mockcat.action.IMPORT_MOCKS" // default
}
```

`mockcatImport` is registered on apply; configure properties before running the task.

When changing public API or JSON shapes, update `mockcat-api` and any export/import paths in `RoomMockcatStore` and tests.

## CI

`.github/workflows/ci.yml` runs ktlint, detekt, Android assemble, and key compile tasks. The `ios` job is disabled (`if: false`) until you enable a macOS runner; flip it and add `xcodebuild` when the Xcode project exists.
