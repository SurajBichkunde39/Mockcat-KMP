package com.mockcat.gradle

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Apply `id = "com.mockcat.mockcat-gradle"` to register the [MockcatImportTask] as `mockcatImport`.
 *
 * Defaults provided automatically:
 * - `adbExecutable` — resolved from `$ANDROID_HOME/platform-tools/adb`; falls back to `adb` on PATH.
 * - `deviceSerial` — read from `ANDROID_SERIAL` env var (convention, always overridable by explicit config).
 * - `mockFiles` — all `*.json` files under a `mocks/` directory next to `build.gradle.kts`.
 * - `applicationId` — read from the Android AGP `ApplicationExtension` if the plugin is applied.
 * - `broadcastAction` — `com.mockcat.action.IMPORT_MOCKS` (overridable).
 *
 * Override any of these in the consuming module as needed.
 */
class MockcatPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val adbFromEnv =
            System
                .getenv("ANDROID_HOME")
                ?.let { home -> File(home, "platform-tools/adb") }
                ?.takeIf { it.isFile }

        target.tasks.register("mockcatImport", MockcatImportTask::class.java) { task ->
            task.group = "mockcat"
            task.description = "Push mock JSON files to a connected device and broadcast import to the app"

            task.broadcastAction.convention("com.mockcat.action.IMPORT_MOCKS")

            if (adbFromEnv != null) {
                task.adbExecutable.convention(target.layout.file(target.provider { adbFromEnv }))
            }

            // convention() lets the consumer's explicit deviceSerial.set(...) always win.
            System.getenv("ANDROID_SERIAL")?.let { task.deviceSerial.convention(it) }

            // Default: all *.json files under mocks/ next to build.gradle.kts.
            // Clients can extend with mockFiles.from(...) or replace with mockFiles.setFrom(...).
            task.mockFiles.from(
                target.fileTree(target.layout.projectDirectory.dir("mocks")) {
                    it.include("**/*.json")
                },
            )
        }

        // Auto-detect applicationId from AGP once all plugins have been applied.
        target.afterEvaluate {
            val appId = runCatching {
                target.extensions.findByType(ApplicationExtension::class.java)
                    ?.defaultConfig
                    ?.applicationId
            }.getOrNull()

            if (appId != null) {
                target.tasks.named("mockcatImport", MockcatImportTask::class.java) { task ->
                    task.applicationId.convention(appId)
                }
            }
        }
    }
}
