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
| `mockcat-intercept-urlsession` | iOS `NSURLRequest` → `HttpRequestMetadata` |
| `mockcat-integration-chucker` | Android: Chucker share text parser for import |
| `mockcat-gradle-plugin` | JVM plugin: ADB + broadcast import task (consumers register/configure) |
| `sample-compose` | Android sample app |

## Conventions

- **Tooling** (see `gradle/libs.versions.toml`): **Gradle 9.3.1**, **AGP 9.1.1**, **Kotlin 2.2.21** with matching **KSP**, **Room 2.8.4**, **Compose Multiplatform** plugin `1.10.3` with **Material3** on its own line (`composeMaterial3`, e.g. `1.10.0-alpha05` — JetBrains does not version Material3 in lockstep with the plugin). `gradle.properties` sets `android.builtInKotlin=false` and `android.newDsl=false` so existing KMP modules can keep `com.android.library` + `kotlin multiplatform` until you migrate to `com.android.kotlin.multiplatform.library` (required long-term for AGP 9+). **KSP** must match the Kotlin version; Room Apple klibs need a matching Kotlin/Native ABI.
- **Quality**: `./gradlew ktlintCheck detekt` (and `ktlintFormat` as needed). Generated output is excluded from ktlint via a root `subprojects` ktlint `filter` with a `FileTreeElement` `exclude` that omits files whose path is under a `build/` directory. Root `.editorconfig` sets `ktlint_function_naming_ignore_when_annotated_with=Composable,Preview` for Compose.
- **Libraries** stay free of Koin/Hilt/Dagger; apps/samples wire dependencies explicitly.
- **Publishing**: several modules use `publishAllLibraryVariants()`; migrating to explicit `publishLibraryVariants` is a follow-up when publishing is finalized.

## Common tasks

- Android debug build: `./gradlew :sample-compose:assembleDebug`
- iOS KSP (persistence): `./gradlew :mockcat-persistence:kspKotlinIosSimulatorArm64` (or `iosArm64`)

When changing public API or JSON shapes, update `mockcat-api` and any export/import paths in `RoomMockcatStore` and tests.
