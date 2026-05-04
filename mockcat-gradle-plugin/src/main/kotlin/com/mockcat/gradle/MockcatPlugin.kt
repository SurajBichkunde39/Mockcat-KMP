package com.mockcat.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Apply [id] = `com.mockcat.mockcat-gradle` and register the [MockcatImportTask] as `mockcatImport`.
 * Configure [MockcatImportTask.adbExecutable], [MockcatImportTask.applicationId], and
 * [MockcatImportTask.mockFiles] in the consumer (see [AGENT.md]).
 */
class MockcatPlugin : Plugin<Project> {
    @Suppress("UnnecessaryApply", "EmptyFunctionBlock")
    override fun apply(target: Project) {
        val adbFromEnv =
            System
                .getenv("ANDROID_HOME")
                ?.let { home -> File(home, "platform-tools/adb") }
                ?.takeIf { it.isFile }
        target.tasks.register("mockcatImport", MockcatImportTask::class.java) { task ->
            task.group = "mockcat"
            task.description = "Push mock JSON to /data/local/tmp and broadcast import to the app"
            if (adbFromEnv != null) {
                task.adbExecutable.set(adbFromEnv)
            }
        }
    }
}
