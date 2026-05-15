import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}
kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.navigation3.runtime)
                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)
                implementation(libs.androidxActivityCompose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.composeMaterial3)
                implementation(libs.composeFoundation)
                implementation(libs.composeUi)
                implementation(libs.okhttp)
                implementation(libs.ktorClientContentNegotiation)
                implementation(libs.ktorClientCore)
                implementation(libs.ktorClientOkhttp)
                implementation(libs.ktorSerializationKotlinxJson)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

// Mockcat is a debug-only tool and must never reach production builds.
// debugImplementation pulls in the real implementation; releaseImplementation swaps in the no-op.
dependencies {
    debugImplementation(project(":mockcat-logger-okhttp"))
    debugImplementation(project(":mockcat-intercept-okhttp"))
    debugImplementation(project(":mockcat-logger-ktor"))
    debugImplementation(project(":mockcat-intercept-ktor"))
    debugImplementation(project(":mockcat-logger-ui"))
    debugImplementation(project(":mockcat-intercept-ui"))
    releaseImplementation(project(":mockcat-noop-android"))
}

android {
    namespace = "com.mockcat.sample"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        applicationId = "com.mockcat.sample"
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    packaging { resources { excludes += setOf("META-INF/**") } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes { getByName("debug") { isDefault = true } }
}
detekt { config.setFrom(rootProject.file("config/detekt.yml")) }

// Push sample mocks to a connected device or emulator.
// URLs in mocks/movies.json match MovieConfig.BASE_URL (default: http://10.0.2.2:8080 for emulator).
// Run: ./gradlew :sample-compose:mockcatImport
// For multiple devices: set ANDROID_SERIAL env var or pass -PandroidSerial=<serial>.
tasks.register("mockcatImport") {
    group = "mockcat"
    description = "Push sample mock JSON files to the device and broadcast import to the app"
    // Capture project state at configuration time (required for configuration cache compatibility).
    val mockFiles = project.fileTree("mocks") { include("**/*.json") }
    val androidSerialProp = project.findProperty("androidSerial") as String?
    inputs.files(mockFiles)
    doLast {
        val adb = System.getenv("ANDROID_HOME")
            ?.let { "$it/platform-tools/adb" }
            ?: "adb"

        // Resolve target device serial.
        val serial: String? = run {
            val explicit = System.getenv("ANDROID_SERIAL") ?: androidSerialProp
            if (explicit != null) return@run explicit

            val devicesOut = ProcessBuilder(adb, "devices").start().inputStream.bufferedReader().readText()
            val connected = devicesOut.lineSequence().drop(1).filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val tab = line.indexOf('\t')
                    if (tab == -1) {
                        null
                    } else if (line.substring(tab + 1).trim() == "device") {
                        line.substring(0, tab).trim()
                    } else {
                        null
                    }
                }.toList()
            when (connected.size) {
                0 -> error("No devices or emulators connected. Connect a device or start an emulator, then retry.")
                1 -> {
                    logger.lifecycle("Target device: ${connected[0]}")
                    connected[0]
                }
                else -> {
                    val list = connected.mapIndexed { i, s -> "  ${i + 1}. $s" }.joinToString("\n")
                    error(
                        "Multiple devices connected. Specify a target:\n" +
                            "  ANDROID_SERIAL=<serial> ./gradlew :sample-compose:mockcatImport\n" +
                            "  ./gradlew :sample-compose:mockcatImport -PandroidSerial=<serial>\n" +
                            "Connected devices:\n$list",
                    )
                }
            }
        }

        val remote = "/data/local/tmp/mockcat_import_data.json"

        val files = mockFiles.files.filter { it.isFile }
        if (files.isEmpty()) {
            logger.warn("No mock files found in mocks/")
            return@doLast
        }

        // Each file is already a valid MultipleMockEntries JSON {"entries":[...]}.
        // Single file: use content as-is. Multiple files: merge by extracting each entries array.
        val payload = if (files.size == 1) {
            files.first().readText(Charsets.UTF_8)
        } else {
            val merged = files.joinToString(",") { f ->
                val json = f.readText(Charsets.UTF_8)
                val arrStart = json.indexOf('[', json.indexOf("\"entries\""))
                val arrEnd = json.lastIndexOf(']')
                json.substring(arrStart + 1, arrEnd).trim()
            }
            "{\"entries\":[$merged]}"
        }

        val tmpFile = File.createTempFile("mockcat", ".json")
        tmpFile.writeText(payload, Charsets.UTF_8)

        fun runCmd(vararg args: String) {
            val cmd = buildList {
                add(adb)
                if (serial != null) {
                    add("-s")
                    add(serial)
                }
                addAll(args)
            }
            val exit = ProcessBuilder(cmd).inheritIO().start().waitFor()
            check(exit == 0) { "Command failed (exit $exit): ${cmd.joinToString(" ")}" }
        }

        fun runCmdIgnoreExit(vararg args: String) {
            val cmd = buildList {
                add(adb)
                if (serial != null) {
                    add("-s")
                    add(serial)
                }
                addAll(args)
            }
            ProcessBuilder(cmd).inheritIO().start().waitFor()
        }

        var pushed = false
        try {
            runCmd("push", tmpFile.absolutePath, remote)
            pushed = true
            runCmd(
                "shell", "am", "broadcast",
                "-a", "com.mockcat.action.IMPORT_MOCKS",
                "-p", "com.mockcat.sample",
                "--es", "mock_file_path", remote,
                "--include-stopped-packages",
            )
        } finally {
            tmpFile.delete()
            if (pushed) runCmdIgnoreExit("shell", "rm", remote)
        }
    }
}
