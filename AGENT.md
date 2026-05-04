# Mockcat (agent context)

**Mockcat** is a Kotlin Multiplatform monorepo for HTTP mocking: shared API and storage, platform interceptors, optional UI, and tooling.

## Layout

| Module | Role |
|--------|------|
| `mockcat-api` | Public types: `HttpRequestMetadata`, `MockEntry`, `MockcatStore`, `MockMatcher`, headers |
| `mockcat-persistence` | Room (KMP) + `RoomMockcatStore`, bundled SQLite, import/export JSON |
| `mockcat-ui` | Compose Multiplatform UI + `MockcatViewModel` (manual wiring, **no DI framework in libraries**) |
| `mockcat-intercept-okhttp` | `MockcatOkHttpInterceptor` |
| `mockcat-intercept-ktor` | `MockcatKtor.createHttpClient` (OkHttp engine) |
| `mockcat-intercept-urlsession` | iOS: `NSURLRequest` → `HttpRequestMetadata`, `runMockcatUrlSessionResolve` for `URLProtocol` / Swift |
| `mockcat-integration-chucker` | Android: Chucker share text parser for import |
| `mockcat-gradle-plugin` | JVM plugin: ADB + broadcast import task (consumers register/configure) |
| `sample-compose` | Android sample app (OkHttp demo + Mockcat UI) |
| `iosApp/MockcatSample` | Native SwiftUI sources (Xcode project you create; link KMP frameworks) |

## Conventions

- **Tooling** (see `gradle/libs.versions.toml`): **Gradle 9.3+**, **AGP 9.1.1**, **Kotlin 2.2.21**, **KSP 2.3.7** (KSP 2.3+ is required for AGP 9 + the Android KMP library plugin; see [google/ksp#2476](https://github.com/google/ksp/issues/2476)). **Room 2.8.4** with the Room Gradle plugin only in `mockcat-persistence` (see below). **Compose Multiplatform** plugin `1.10.3` with **Material3** on its own line (`composeMaterial3`).

- **Android KMP library plugin** (most modules): `com.android.kotlin.multiplatform.library` with a `kotlin { android { namespace; compileSdk; minSdk; compilerOptions { jvmTarget } } }` block. There is **no** top-level `android { }` in those modules. **Android main** compile task name is e.g. `:mockcat-api:compileAndroidMain` (single-variant plugin; not `compileDebugKotlinAndroid`).

- **`mockcat-persistence` exception:** still uses `com.android.library` + `androidTarget { publishAllLibraryVariants() }` and the **`androidx.room` Gradle plugin** because the Room plugin is not yet compatible with `com.android.kotlin.multiplatform.library` (class cast on `KotlinMultiplatformAndroidCompilationImpl`). KSP for Room uses `kspAndroid` and `kspKotlinIos*`. Revisit when AndroidX Room supports the new Android KMP target.

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
- iOS KSP (persistence): `./gradlew :mockcat-persistence:kspKotlinIosSimulatorArm64` (or `kspKotlinIosArm64`)
- iOS **frameworks** for the Swift app (simulator arm64, debug):
  - `./gradlew :mockcat-api:linkDebugFrameworkIosSimulatorArm64`
  - `./gradlew :mockcat-persistence:linkDebugFrameworkIosSimulatorArm64`
  - `./gradlew :mockcat-intercept-urlsession:linkDebugFrameworkIosSimulatorArm64`
- Frameworks are emitted under each module’s `build/` tree (see Gradle task output for exact paths). In Xcode, **Embed & Sign** those frameworks, then import in Swift, e.g. `MockcatApi`, `MockcatPersistence`, `MockcatInterceptUrlsession`. The SQLite DB for Room on iOS is under the app’s documents directory (see `DatabaseBuilder.ios.kt`).

## iOS sample (Swift)

- **Sources:** `iosApp/MockcatSample/`. There is no checked-in `.xcodeproj`—create an iOS app target in Xcode, add these Swift files, and link the three frameworks from Gradle.
- **Kotlin:** `getMockcatStoreForIos()` in `mockcat-persistence`, `RunMockcatUrlSessionResolve` / `toHttpRequestMetadata` in `mockcat-intercept-urlsession` for a Swift `URLProtocol` implementation.
- **URLSession:** set `URLSessionConfiguration.protocolClasses` to your `URLProtocol` **before** creating a `URLSession` that should see mocks.

## Android `sample-compose`

- `OkHttpSample` + `MainActivity` use `MockcatOkHttpInterceptor` with a dedicated demo URL; the UI adds a static mock in `LaunchedEffect` so the request is short-circuited without hitting the network.

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
