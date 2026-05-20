# Gradle Plugin

The `mockcat-gradle-plugin` registers a `mockcatImport` task that reads JSON mock files from your project and pushes them to a connected Android device or emulator over ADB — atomically replacing all existing mocks.

---

## Apply the plugin

```kotlin title="app/build.gradle.kts"
plugins {
    id("com.mockcat.mockcat-gradle") version "0.1.0"
}
```

The plugin auto-configures everything by reading from the Android Gradle Plugin extension. No additional configuration is required for the common case.

---

## Run the import

```bash
./gradlew mockcatImport
```

This:

1. Reads all `*.json` files from `app/mocks/`
2. Parses them into a merged list of mock entries
3. Pushes the payload to `/data/local/tmp/mockcat_import_data.json` on the device
4. Broadcasts `com.mockcat.action.IMPORT_MOCKS` to your app
5. The `MockcatImportReceiver` in your app receives it, reads the file, and replaces all mocks atomically
6. Cleans up the temp file on the device

The receiver is declared in `mockcat-intercept-persistence`'s merged manifest — no manual registration needed.

---

## Mocks directory

By default the plugin looks for `*.json` files in `app/mocks/` (relative to the module that applies the plugin). You can organize files however you like — one file per endpoint, one per feature, or all in one:

```
app/
  mocks/
    auth.json
    users.json
    products/
      list.json
      detail.json
```

All JSON files in the directory (including subdirectories) are merged and imported as one batch.

---

## Configuration reference

All properties have defaults. Override only what you need:

```kotlin title="app/build.gradle.kts"
tasks.named<com.mockcat.gradle.MockcatImportTask>("mockcatImport") {
    // Path to adb binary. Default: $ANDROID_HOME/platform-tools/adb, then adb on PATH.
    adbExecutable.set(file("/path/to/adb"))

    // Application ID of the target app. Default: auto-read from AGP defaultConfig.
    applicationId.set("com.example.app")

    // Source mock files. Default: all *.json under app/mocks/.
    mockFiles.setFrom(fileTree("custom-mocks") { include("**/*.json") })

    // ADB device serial. Default: $ANDROID_SERIAL env var.
    deviceSerial.set("emulator-5554")
}
```

| Property | Default | Override |
|----------|---------|----------|
| `adbExecutable` | `$ANDROID_HOME/platform-tools/adb`, then `adb` on PATH | `adbExecutable.set(file(...))` |
| `applicationId` | Read from AGP `ApplicationExtension.defaultConfig` | `applicationId.set("com.example.app")` |
| `mockFiles` | All `*.json` under `mocks/` | `mockFiles.setFrom(...)` |
| `deviceSerial` | `$ANDROID_SERIAL` env var | `deviceSerial.set("emulator-5554")` |

---

## Multiple connected devices

When exactly one device is connected, the task auto-selects it. When multiple devices are connected, you must specify a target.

**Via environment variable (preferred for ad-hoc use):**

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew mockcatImport
```

**Via Gradle configuration (for a fixed device in your workflow):**

```kotlin
tasks.named<com.mockcat.gradle.MockcatImportTask>("mockcatImport") {
    deviceSerial.set("emulator-5554")
}
```

**Error when multiple devices and no serial specified:**

```
> FAILURE: Build failed with an exception.
  Multiple devices connected. Specify a target device:
    ANDROID_SERIAL=<serial> ./gradlew mockcatImport
    ...
  Connected devices:
    emulator-5554
    emulator-5556
```

**Error when no devices connected:**

```
> FAILURE: Build failed with an exception.
  No devices or emulators connected.
  Connect a device or start an emulator, then retry.
```

---

## Import is always a full replace

Every `mockcatImport` run atomically replaces **all** existing mocks. The database is cleared and repopulated in a single Room transaction. This makes your `mocks/` directory the single source of truth — you never get leftover rules from a previous import.

!!! tip
    Any mocks created manually in the in-app editor will also be cleared on the next import. If you want to keep editor-created rules, export them first via the editor's export function, add the JSON to your `mocks/` directory, then re-import.

---

## Using with CI / pre-launch scripts

The Gradle plugin is designed to work in automation. A typical pre-launch script pattern:

```bash
#!/bin/bash
# Start the emulator, install the app, then push mocks
./gradlew :sample-compose:installDebug
./gradlew mockcatImport
```

Because the task is `@UntrackedTask`, Gradle never caches its output — it always runs when invoked, which is the right behavior for a device-side operation.
