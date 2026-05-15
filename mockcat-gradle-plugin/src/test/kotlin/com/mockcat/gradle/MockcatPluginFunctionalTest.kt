package com.mockcat.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Functional tests for MockcatPlugin using Gradle TestKit.
 *
 * Each test spins up a real Gradle build in a temp directory with the plugin applied,
 * exercising the same code path an external consumer would hit.
 */
class MockcatPluginFunctionalTest {

    @get:Rule
    val projectDir = TemporaryFolder()

    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @Before
    fun setup() {
        settingsFile = projectDir.newFile("settings.gradle.kts")
        settingsFile.writeText("""rootProject.name = "test-consumer"""")
        buildFile = projectDir.newFile("build.gradle.kts")
    }

    // region — plugin application

    @Test
    fun `plugin registers mockcatImport task in the mockcat group`() {
        buildFile.writeText(
            """
            plugins { id("com.mockcat.mockcat-gradle") }
            tasks.named<com.mockcat.gradle.MockcatImportTask>("mockcatImport") {
                applicationId.set("com.example.app")
            }
            """.trimIndent(),
        )

        val result = runner("tasks", "--group=mockcat").build()

        assertTrue(
            "mockcatImport should appear under the mockcat task group",
            result.output.contains("mockcatImport"),
        )
    }

    @Test
    fun `mockcatImport task is in the mockcat group`() {
        buildFile.writeText(
            """
            plugins { id("com.mockcat.mockcat-gradle") }
            tasks.named<com.mockcat.gradle.MockcatImportTask>("mockcatImport") {
                applicationId.set("com.example.app")
            }
            """.trimIndent(),
        )

        val result = runner("help", "--task", "mockcatImport").build()

        // Gradle formats group as "Group\n     mockcat" (label on one line, value on the next).
        assertTrue(
            "Output should mention 'mockcat' group. Output was:\n${result.output}",
            result.output.contains("mockcat"),
        )
    }

    // endregion

    // region — zero-devices error

    @Test
    fun `task fails with clear message when no devices are connected`() {
        val adb = fakeAdb(devices = emptyList())
        buildFile.writeText(pluginBuildScript(adb = adb))
        writeMockFile()

        val result = runner("mockcatImport").buildAndFail()

        assertTrue(
            "Should mention 'No devices'. Output was:\n${result.output}",
            result.output.contains("No devices") || result.output.contains("no device"),
        )
    }

    // endregion

    // region — single device auto-selection

    @Test
    fun `task auto-selects the single connected device`() {
        val adb = fakeAdb(devices = listOf("emulator-5554"), respondToBroadcast = true)
        buildFile.writeText(pluginBuildScript(adb = adb))
        writeMockFile()

        val result = runner("mockcatImport").build()

        assertTrue(
            "Should log the auto-selected device serial. Output:\n${result.output}",
            result.output.contains("emulator-5554"),
        )
        assertEquals(TaskOutcome.SUCCESS, result.task(":mockcatImport")?.outcome)
    }

    // endregion

    // region — mock file defaults

    @Test
    fun `task picks up json files from mocks directory by default`() {
        val adb = fakeAdb(devices = listOf("emulator-5554"), respondToBroadcast = true)
        buildFile.writeText(pluginBuildScript(adb = adb))
        writeMockFile()

        val result = runner("mockcatImport").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":mockcatImport")?.outcome)
    }

    @Test
    fun `task skips gracefully when mocks directory is empty`() {
        val adb = fakeAdb(devices = listOf("emulator-5554"))
        buildFile.writeText(pluginBuildScript(adb = adb))
        projectDir.newFolder("mocks") // empty

        val result = runner("mockcatImport").build()

        assertTrue(
            "Should warn and skip. Output:\n${result.output}",
            result.output.contains("No mocks parsed") || result.output.contains("Skipping"),
        )
        assertEquals(TaskOutcome.SUCCESS, result.task(":mockcatImport")?.outcome)
    }

    // endregion

    // region — deviceSerial property

    @Test
    fun `deviceSerial property bypasses auto-detection and targets the configured device`() {
        // Two devices; fake adb only accepts push/broadcast to emulator-5556.
        val adb = fakeAdb(
            devices = listOf("emulator-5554", "emulator-5556"),
            respondToBroadcast = true,
            targetSerial = "emulator-5556",
        )
        buildFile.writeText(pluginBuildScript(adb = adb, deviceSerial = "emulator-5556"))
        writeMockFile()

        val result = runner("mockcatImport").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":mockcatImport")?.outcome)
    }

    // endregion

    // region — helpers

    private fun runner(vararg args: String): GradleRunner = GradleRunner.create()
        .withProjectDir(projectDir.root)
        .withArguments(*args)
        .withPluginClasspath()

    private fun pluginBuildScript(adb: File, deviceSerial: String? = null): String = buildString {
        appendLine("import com.mockcat.gradle.MockcatImportTask")
        appendLine("""plugins { id("com.mockcat.mockcat-gradle") }""")
        appendLine("""tasks.named<MockcatImportTask>("mockcatImport") {""")
        appendLine("""    applicationId.set("com.example.app")""")
        appendLine("""    adbExecutable.set(file("${adb.absolutePath.replace("\\", "\\\\")}"))""")
        if (deviceSerial != null) appendLine("""    deviceSerial.set("$deviceSerial")""")
        appendLine("}")
    }

    private fun writeMockFile(dir: String = "mocks") {
        val mocksDir = projectDir.newFolder(dir)
        File(mocksDir, "sample.json").writeText(
            """{"entries":[{"url":"https://example.com","httpMethod":"GET","responseCode":200}]}""",
        )
    }

    /**
     * Writes a shell script that behaves like `adb` for the given scenario.
     *
     * `adb devices` returns [devices]; `adb push` and `adb shell rm` succeed;
     * `adb shell am broadcast` prints the standard result line.
     * When [targetSerial] is set, commands targeting any other serial exit 1 so we can
     * verify the task used the expected serial.
     */
    private fun fakeAdb(
        devices: List<String>,
        respondToBroadcast: Boolean = false,
        targetSerial: String? = null,
    ): File {
        val devicesOutput = buildString {
            appendLine("List of devices attached")
            devices.forEach { appendLine("$it\tdevice") }
        }.trim()

        val script = buildString {
            appendLine("#!/bin/sh")
            appendLine("serial=\"\"")
            appendLine("while [ \"\$1\" = \"-s\" ]; do serial=\"\$2\"; shift 2; done")
            appendLine("cmd=\"\$1\"; shift")
            appendLine("case \"\$cmd\" in")
            appendLine("  devices)")
            appendLine("    printf '%s\\n' '$devicesOutput'")
            appendLine("    exit 0 ;;")
            appendLine("  push)")
            if (targetSerial != null) appendLine("    [ \"\$serial\" = \"$targetSerial\" ] || exit 1")
            appendLine("    exit 0 ;;")
            appendLine("  shell)")
            if (targetSerial != null) appendLine("    [ \"\$serial\" = \"$targetSerial\" ] || exit 1")
            if (respondToBroadcast) {
                appendLine("    echo 'Broadcast completed: result=0'")
            }
            appendLine("    exit 0 ;;")
            appendLine("  *) exit 0 ;;")
            appendLine("esac")
        }

        val file = projectDir.newFile("fake-adb.sh")
        file.writeText(script)
        file.setExecutable(true)
        return file
    }

    // endregion
}
