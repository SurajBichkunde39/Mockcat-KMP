# Changelog

## 0.1.0 — Initial release

**Intercept**

- OkHttp interceptor (`mockcat-intercept-okhttp`) with static and redirect mock resolution
- Ktor `HttpSend` plugin (`mockcat-intercept-ktor`) with synthetic call support
- iOS URLSession bridge (`mockcat-intercept-urlsession`) via Kotlin/Native
- Room-backed mock store (`mockcat-intercept-persistence`) with atomic import/replace
- `MockcatImportReceiver` auto-declared in library manifest — no app-side registration needed
- Header and query parameter constraint matching with priority (more constraints = higher specificity)
- Artificial response delay support (`delayMs`)
- Redirect mock type (`mockType: REDIRECT`) with loop prevention via `X-Mockcat-Redirected` header

**Logging**

- OkHttp logging interceptor (`mockcat-logger-okhttp`) with configurable body capture limit (default 256 KB)
- Ktor logging plugin (`mockcat-logger-ktor`) with full body capture
- iOS URLSession logging (`mockcat-logger-urlsession`)
- Room-backed HTTP log store (`mockcat-logger-persistence`) with persistent call history
- `HttpLogReaderRegistry` — process-wide reader reference, decoupled from feature modules
- In-memory log store (`InMemoryHttpLogStore`) as a lightweight alternative (ring buffer, 200 entries)

**UI**

- Compose mock rules editor (`mockcat-intercept-ui`) with CRUD, enable/disable toggles, import/export
- Compose HTTP log list + detail viewer (`mockcat-logger-ui`) with status badges, timing, cURL export
- Android `MockcatActivity` and `HttpLogListActivity` (both `singleTask` with dedicated `taskAffinity`)
- iOS `UIViewController` for HTTP log viewer via `createHttpLogListViewController()`

**Gradle plugin**

- `mockcat-gradle-plugin` with `mockcatImport` task
- Auto-detects `applicationId` from AGP and ADB path from `$ANDROID_HOME`
- Single-device auto-selection; clear error messages for multi-device and no-device cases
- `$ANDROID_SERIAL` environment variable support

**Production safety**

- `mockcat-noop-android` — identical public API, all methods no-ops, empty manifest
- Supports `debugImplementation` / `releaseImplementation` split and product flavor configurations
- No Room database initialized in no-op builds

**Platform targets**

- Android: OkHttp 4.x + Ktor 3.x
- iOS: `iosArm64`, `iosSimulatorArm64`
- Kotlin 2.x, KSP 2.x, Room 2.8.x, Compose Multiplatform 1.x
