# Mockcat sample (Compose, Android)

A small movie browser that shows Mockcat's HTTP logger and mock intercept with OkHttp, plus a plain Ktor client for comparison. It loads data from the in-repo [`sample-server`](../sample-server).

## 1. Start the sample server

From the **repository root**, start the server and leave it running:

```bash
./gradlew :sample-server:run
```

It listens on port `8080`. Check it with `curl http://localhost:8080/api/movies`.

## 2. Install the app

With an emulator running or a device connected over ADB:

```bash
./gradlew :sample-compose:installDebug
```

Then open **Mockcat sample** on the device. You can also run the `sample-compose` configuration from Android Studio. If several devices are connected, pick one with `ANDROID_SERIAL=<serial>` (see `adb devices`).

## 3. Reach the server from the device

The app's base URL is set in [`MovieConfig.kt`](src/main/kotlin/com/mockcat/sample/data/MovieConfig.kt).

- **Emulator** — works as is. `http://10.0.2.2:8080` is the emulator's alias for your machine's `localhost`.
- **Physical device** — forward the port over ADB and point the app at the device's loopback:

  ```bash
  adb reverse tcp:8080 tcp:8080
  ```

  Then set `BASE_URL = "http://127.0.0.1:8080"` in `MovieConfig.kt` and reinstall. Alternatively, use your machine's LAN address.

## 4. Use the app

Pick a client on the launcher screen:

- **OkHttp integration** — requests go through `MockcatLogging` and `MockcatIntercept`.
- **Ktor client** — Ktor on the OkHttp engine with `mockcat-logger-ktor`; requests are logged but not intercepted.

On the movie list, **Logger** opens the HTTP log, **Mocks** opens the mock rule editor, and **Change client** returns to the launcher.

## Optional: push sample mocks

[`mocks/movies.json`](mocks/movies.json) contains mock rules; most URLs use the emulator base URL `http://10.0.2.2:8080`. Push them to the running app with:

```bash
./gradlew :sample-compose:mockcatImport
# several devices: ANDROID_SERIAL=<serial> ./gradlew :sample-compose:mockcatImport
#             or:  ./gradlew :sample-compose:mockcatImport -PandroidSerial=<serial>
```

If you changed `BASE_URL`, update the URLs in the mock file to match.
