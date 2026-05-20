# Installation

## Requirements

- **JDK 17+** (aligned with Android Gradle Plugin)
- **Android Gradle Plugin** 9.x
- **Kotlin** 2.x (KSP version must match; see `gradle/libs.versions.toml`)
- **minSdk 24+**
- **iOS**: Xcode 15+, `iosArm64` or `iosSimulatorArm64` targets

---

## Artifacts

All artifacts share the same group and version:

```
Group:   com.mockcat
Version: 0.1.0
```

| Artifact | What it provides | Platform |
|----------|-----------------|----------|
| `mockcat-api` | Core types: `MockEntry`, `MockcatStore`, `HttpRequestMetadata`, matchers | KMP |
| `mockcat-intercept-okhttp` | OkHttp interceptor | Android |
| `mockcat-intercept-ktor` | Ktor `HttpSend` plugin | Android + JVM |
| `mockcat-intercept-urlsession` | iOS URLSession bridge | iOS |
| `mockcat-intercept-persistence` | Room database for mocks, import receiver | Android |
| `mockcat-intercept-ui` | Compose mock rules editor, `MockcatActivity` | Android |
| `mockcat-logger-core` | `HttpLogReader` / `HttpLogWriter` interfaces, in-memory store | KMP |
| `mockcat-logger-okhttp` | OkHttp logging interceptor | Android |
| `mockcat-logger-ktor` | Ktor logging plugin | Android + JVM |
| `mockcat-logger-urlsession` | iOS URLSession logging | iOS |
| `mockcat-logger-persistence` | Room database for HTTP logs | Android |
| `mockcat-logger-ui` | Compose HTTP log list + detail screens | KMP |
| `mockcat-noop-android` | Same API as all above — all methods are no-ops, empty manifest | Android |
| `mockcat-gradle-plugin` | `mockcatImport` Gradle task for ADB push | JVM (Gradle) |

---

## Android setup

### Step 1 — Add dependencies

Add the real library to debug (or non-production) variants and the no-op to release:

```kotlin title="app/build.gradle.kts"
dependencies {
    // Intercept
    debugImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-intercept-ktor:0.1.0")    // if using Ktor
    debugImplementation("com.mockcat:mockcat-intercept-ui:0.1.0")

    // Logging
    debugImplementation("com.mockcat:mockcat-logger-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-ktor:0.1.0")       // if using Ktor
    debugImplementation("com.mockcat:mockcat-logger-ui:0.1.0")

    // No-op replaces all of the above in production.
    // Same public API — your app code never needs to change.
    releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
}
```

### Step 2 — Apply the Gradle plugin (optional)

If you want to push mocks from JSON files via ADB:

```kotlin title="app/build.gradle.kts"
plugins {
    id("com.mockcat.mockcat-gradle") version "0.1.0"
}
```

No other configuration is required — the plugin auto-detects your `applicationId` and ADB path.

---

## With product flavors

If your project uses flavors, wire Mockcat to whichever configurations are non-production:

```kotlin title="app/build.gradle.kts"
dependencies {
    // QA builds (debug + release) get the real library
    qaDebugImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    qaReleaseImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    // ... other mockcat modules

    // Only production variants get the no-op
    prodDebugImplementation("com.mockcat:mockcat-noop-android:0.1.0")
    prodReleaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
}
```

---

## iOS setup

KMP frameworks are linked as **debug-only** by convention. The Gradle build produces `linkDebugFramework*` targets, which your `project.yml` or Xcode project references.

```yaml title="project.yml (XcodeGen)"
dependencies:
  - framework: path/to/MockcatInterceptUrlsession.framework
    embed: true
    configurations: [Debug]
  - framework: path/to/MockcatLoggerUI.framework
    embed: true
    configurations: [Debug]
```

For a production archive, restrict all Mockcat framework references to the `Debug` configuration so they are excluded from the release build.

!!! note
    A no-op XCFramework for iOS is planned for a future release.

---

## Snapshot builds

Until 1.0, artifacts are published as snapshots. Add the snapshot repository to your `settings.gradle.kts`:

```kotlin title="settings.gradle.kts"
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}
```
