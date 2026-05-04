package com.mockcat.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Apply [id] = `com.mockcat.mockcat-gradle` to register the plugin id; register a
 * [MockcatImportTask] from your `build.gradle.kts` (see [AGENT.md]).
 */
class MockcatPlugin : Plugin<Project> {
    @Suppress("EmptyFunctionBlock", "UnnecessaryApply")
    override fun apply(target: Project) {
    }
}
