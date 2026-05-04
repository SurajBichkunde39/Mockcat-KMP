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

- **Kotlin** / **KSP** / **Room** versions are aligned in `gradle/libs.versions.toml` (KSP must match the Kotlin version). Room iOS klibs require a matching Kotlin native ABI.
- **Quality**: `./gradlew ktlintCheck detekt` (and `ktlintFormat` as needed). Generated output is excluded from ktlint via a root `subprojects` ktlint `filter` with a `FileTreeElement` `exclude` that omits files whose path is under a `build/` directory. Root `.editorconfig` sets `ktlint_function_naming_ignore_when_annotated_with=Composable,Preview` for Compose.
- **Libraries** stay free of Koin/Hilt/Dagger; apps/samples wire dependencies explicitly.
- **Publishing**: several modules use `publishAllLibraryVariants()`; migrating to explicit `publishLibraryVariants` is a follow-up when publishing is finalized.

## Common tasks

- Android debug build: `./gradlew :sample-compose:assembleDebug`
- iOS KSP (persistence): `./gradlew :mockcat-persistence:kspKotlinIosSimulatorArm64` (or `iosArm64`)

When changing public API or JSON shapes, update `mockcat-api` and any export/import paths in `RoomMockcatStore` and tests.
