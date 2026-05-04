# Mockcat sample (SwiftUI, iOS)

The **`MockcatSample.xcodeproj`** in this folder is generated from **`project.yml`** (XcodeGen). You can open it directly in Xcode.

## One-time: XcodeGen (to regenerate the project)

If you change `project.yml` or `link_kmp.sh`, regenerate:

```bash
brew install xcodegen
cd iosApp/MockcatSample
xcodegen generate
```

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

The HTTP log list is empty until something logs into the same store (e.g. URLSession + interceptor work in progress). `installHttpLogReaderForIos` still creates the DB and registry so the UI can open.

## Optional other frameworks

**MockcatApi**, **MockcatPersistence**, **MockcatInterceptUrlsession** for mocks and `URLSession` (see [AGENT.md](../../AGENT.md)); not required for the log viewer only.
