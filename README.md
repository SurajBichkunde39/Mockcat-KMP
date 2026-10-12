# Mockcat

[![CI](https://github.com/SurajBichkunde39/Mockcat-KMP/actions/workflows/ci.yml/badge.svg)](https://github.com/SurajBichkunde39/Mockcat-KMP/actions/workflows/ci.yml)

**Mockcat** is a Kotlin Multiplatform toolkit for HTTP mocking, traffic inspection, and interactive mock rule editing — without touching your production code.

📖 **[Full documentation →](https://surajbichkunde39.github.io/Mockcat-KMP/)**

---

## What it does

- **Intercept** — Return saved responses or redirect requests at runtime. Rules are stored in a local database, editable in a built-in Compose UI.
- **Log** — Capture every HTTP call (headers, body, timing) and inspect it in-app without a proxy.
- **Push mocks over ADB** — The Gradle plugin reads JSON files from your project and loads them into a connected device in one command.

Supports Android (OkHttp, Ktor) and iOS (URLSession).

---

## Screenshots

### HTTP Logger

<table>
  <tr>
    <td align="center"><b>Log List</b></td>
    <td align="center"><b>Overview</b></td>
    <td align="center"><b>Request</b></td>
    <td align="center"><b>Response</b></td>
  </tr>
  <tr>
    <td>
      <img src="assets/screenshots/logger_list.png#gh-light-mode-only" width="200"/>
      <img src="assets/screenshots/logger_list_dark.png#gh-dark-mode-only" width="200"/>
    </td>
    <td>
      <img src="assets/screenshots/logger_detail_overview.png#gh-light-mode-only" width="200"/>
      <img src="assets/screenshots/logger_detail_overview_dark.png#gh-dark-mode-only" width="200"/>
    </td>
    <td>
      <img src="assets/screenshots/logger_detail_request.png#gh-light-mode-only" width="200"/>
      <img src="assets/screenshots/logger_detail_request_dark.png#gh-dark-mode-only" width="200"/>
    </td>
    <td>
      <img src="assets/screenshots/logger_detail_response.png#gh-light-mode-only" width="200"/>
      <img src="assets/screenshots/logger_detail_response_dark.png#gh-dark-mode-only" width="200"/>
    </td>
  </tr>
</table>

### Mock Intercept

<table>
  <tr>
    <td align="center"><b>Mock Rules</b></td>
    <td align="center"><b>Edit Rule</b></td>
  </tr>
  <tr>
    <td>
      <img src="assets/screenshots/intercept_list.png#gh-light-mode-only" width="200"/>
      <img src="assets/screenshots/intercept_list_dark.png#gh-dark-mode-only" width="200"/>
    </td>
    <td>
      <img src="assets/screenshots/intercept_edit.png#gh-light-mode-only" width="200"/>
      <img src="assets/screenshots/intercept_edit_dark.png#gh-dark-mode-only" width="200"/>
    </td>
  </tr>
</table>

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
val mockcatIntercept = MockcatIntercept(context)
val mockcatLogger = MockcatLogging(context)

val client = OkHttpClient.Builder()
    .addInterceptor(mockcatLogger)
    .addInterceptor(mockcatIntercept)
    .build()
    .also { mockcatIntercept.bindClient(it) }
```

```bash
./gradlew mockcatImport   # push mocks from app/mocks/*.json to connected device
```

See the **[Quick Start guide](https://surajbichkunde39.github.io/Mockcat-KMP/getting-started/quick-start/)** for the full walkthrough.

---

## Documentation

Full docs at **[surajbichkunde39.github.io/Mockcat-KMP](https://surajbichkunde39.github.io/Mockcat-KMP/)** — installation, guides for OkHttp/Ktor/URLSession, mock file format, Gradle plugin, architecture, and more.

---

## Requirements

- JDK 17+, Android Gradle Plugin 9.x, Kotlin 2.x, minSdk 24+
- iOS: Xcode 15+, `iosArm64` / `iosSimulatorArm64`

## Contributing

Prefer small, focused changes. Keep `commonMain` free of platform types in `mockcat-api`. After Kotlin edits, run `./gradlew ktlintCheck detekt`. Open issues or pull requests against the default branch.

`main` is protected by the ruleset in [`.github/rulesets/main-protection.json`](.github/rulesets/main-protection.json): changes land only through pull requests that have one approval, resolved review threads, and green `CI` checks on a branch that is up to date with `main`. To restore it, import that file under **Settings → Rules → Rulesets → New ruleset → Import a ruleset**.
