# Production Builds

Mockcat must not ship in production builds. The `mockcat-noop-android` artifact provides the exact same public API as all other Mockcat modules — with all methods as no-ops and an empty manifest — so your app code never needs to change between environments.

---

## How it works

When you add `releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")`, the no-op artifact replaces every Mockcat module in release builds. The API surface is identical, so `MockcatIntercept(context)`, `MockcatLogging(context)`, `MockcatUi.createLaunchIntent(context)`, etc. all still compile — they just do nothing at runtime.

```kotlin
// This code compiles and runs safely in both debug and release:
val mockcatIntercept = MockcatIntercept(context)
val mockcatLogger = MockcatLogging(context)

val client = OkHttpClient.Builder()
    .addInterceptor(mockcatLogger)
    .addInterceptor(mockcatIntercept)
    .build()
    .also { mockcatIntercept.bindClient(it) }
```

In debug: requests are intercepted and logged.
In release: `MockcatLogging` and `MockcatIntercept` call `chain.proceed(request)` immediately. Zero overhead.

---

## What the no-op guarantees

| Guarantee | Detail |
|-----------|--------|
| No activities in manifest | `MockcatActivity` and `HttpLogListActivity` are not declared |
| No receivers in manifest | `MockcatImportReceiver` is not declared |
| No permissions | `INTERNET` is not added by Mockcat (it's in your app manifest anyway if you make network calls) |
| No Room database | `ProcessMockcatStore` and `ProcessHttpLogStore` are never initialized |
| No log capture | `observeLogs()` returns an empty `Flow` |
| No interception | All requests pass through the OkHttp chain unmodified |
| No Ktor plugin work | `MockcatKtorIntercept` and `MockcatKtorHttpLogging` install empty plugins |

---

## Basic setup

```kotlin title="app/build.gradle.kts"
dependencies {
    // Debug / development builds
    debugImplementation("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-intercept-ktor:0.1.0")
    debugImplementation("com.mockcat:mockcat-intercept-ui:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-okhttp:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-ktor:0.1.0")
    debugImplementation("com.mockcat:mockcat-logger-ui:0.1.0")

    // Production — replaces everything above, same API
    releaseImplementation("com.mockcat:mockcat-noop-android:0.1.0")
}
```

---

## With product flavors

If your project uses flavors, map each non-production configuration to the real library and each production configuration to the no-op:

```kotlin title="app/build.gradle.kts"
dependencies {
    // QA builds (any variant of the "qa" flavor)
    "qaDebugImplementation"("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    "qaReleaseImplementation"("com.mockcat:mockcat-intercept-okhttp:0.1.0")
    "qaDebugImplementation"("com.mockcat:mockcat-intercept-ui:0.1.0")
    "qaReleaseImplementation"("com.mockcat:mockcat-intercept-ui:0.1.0")
    // ... repeat for other modules

    // Production (any variant of the "prod" flavor)
    "prodDebugImplementation"("com.mockcat:mockcat-noop-android:0.1.0")
    "prodReleaseImplementation"("com.mockcat:mockcat-noop-android:0.1.0")
}
```

---

## iOS production

For iOS, restrict KMP framework dependencies to the `Debug` configuration:

```yaml title="project.yml (XcodeGen)"
targets:
  MyApp:
    dependencies:
      - framework: path/to/MockcatInterceptUrlsession.framework
        embed: true
        configurations: [Debug]
      - framework: path/to/MockcatLoggerUI.framework
        embed: true
        configurations: [Debug]
```

With this setup, `Archive` builds (used for App Store submission) do not include the Mockcat frameworks. Wrap the Swift call sites in a `#if DEBUG` guard:

```swift
#if DEBUG
installHttpLogReaderForIos()
#endif
```

!!! note
    A no-op XCFramework for iOS is planned. It will let you remove the `#if DEBUG` guards on the Swift side, mirroring the Android no-op experience.

---

## Verifying the no-op in a release build

To confirm Mockcat has no footprint in your release APK:

```bash
# Check for Mockcat activities in the merged manifest
./gradlew :app:processReleaseManifest
grep -i "mockcat" app/build/intermediates/merged_manifests/release/AndroidManifest.xml
# Should produce no output

# Check the release APK for Mockcat classes
unzip -p app/build/outputs/apk/release/app-release.apk | strings | grep -i mockcat
# Should produce no output (or only string resources if any)
```
