# Mockcat sample (SwiftUI, iOS)

The **`MockcatSample.xcodeproj`** in this folder is generated from **`project.yml`** (XcodeGen). You can open it directly in Xcode.

## One-time: XcodeGen (to regenerate the project)

If you change `project.yml` or `link_kmp.sh`, regenerate:

```bash
brew install xcodegen
cd iosApp/MockcatSample
xcodegen generate
```

## Start the sample server

The app fetches movies from the in-repo Ktor server. From the **repository root**, start it and leave it running:

```bash
./gradlew :sample-server:run
```

Check it with `curl http://localhost:8080/api/movies`. The simulator shares the Mac's network, so the app's default base URL `http://localhost:8080` works as is. On a **physical device**, enter your Mac's LAN address (e.g. `http://192.168.1.20:8080`) in the app's *Sample server base URL* field.

## Open and run (simulator)

1. **Java / Gradle** — the app must be able to run `./gradlew` from the **repository root** (Xcode’s *Link KMP (Gradle)* pre-build script uses `PROJECT_DIR` to find it).
2. In Xcode: **File → Open** → `iosApp/MockcatSample/MockcatSample.xcodeproj`.
3. Select a **simulator** (e.g. iPhone 16) as the run destination. Use an **Apple Silicon** Mac with **arm64** simulators; the KMP framework path is `iosSimulatorArm64`.
4. **Run** (⌘R). The pre-build step runs  
   `:mockcat-logger-ui:linkDebugFrameworkIosSimulatorArm64`  
   the first time this can take a few minutes.

## Kotlin → Swift names

The framework header (`MockcatLoggerUI.framework/Headers/MockcatLoggerUI.h`) exposes file-level Kotlin APIs as `*Kt` **class methods**, for example:

- `InstallHttpLogReaderForIosKt.installHttpLogReaderForIos()`
- `HttpLogListViewControllerKt.createHttpLogListViewController()`

## KMP build only (no Xcode)

From the repo root:

```bash
./gradlew :mockcat-logger-ui:linkDebugFrameworkIosSimulatorArm64
```

Output: `mockcat-logger-ui/build/bin/iosSimulatorArm64/debugFramework/MockcatLoggerUI.framework`

## Device builds

The Xcode project is wired for the **simulator** framework. For a **physical device**, run  
`linkDebugFrameworkIosArm64` (and `kspKotlinIosArm64` for Room), then point **Framework Search Paths** at  
`mockcat-logger-ui/build/bin/iosArm64/debugFramework` (or the path Gradle prints).

## Runtime

Tap **Fetch movies** to load the list from the sample server, then open a movie for its details. Each `URLSession` call is recorded through `MockcatLoggerUrlSession` (see `Api.swift`); tap **HTTP log** in the toolbar to inspect them. If the fetch fails, make sure the sample server is running and the base URL is correct.

## Optional other frameworks

**MockcatApi**, **MockcatPersistence**, **MockcatInterceptUrlsession** for mocks and `URLSession` (see [AGENT.md](../../AGENT.md)); not required for the log viewer only.
