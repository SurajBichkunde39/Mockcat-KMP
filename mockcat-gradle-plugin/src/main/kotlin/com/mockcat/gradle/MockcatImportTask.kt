package com.mockcat.gradle

import com.mockcat.api.MockFileEntry
import com.mockcat.api.MultipleMockEntries
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@UntrackedTask(because = "Pushes mock data to a connected ADB device — outputs live on the device, not the filesystem")
abstract class MockcatImportTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    @get:Optional
    abstract val adbExecutable: RegularFileProperty

    @get:InputFiles
    abstract val mockFiles: ConfigurableFileCollection

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    @get:Optional
    abstract val deviceSerial: Property<String>

    @get:Input
    abstract val broadcastAction: Property<String>

    private val json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }

    @TaskAction
    fun importRun() {
        val adb = if (adbExecutable.isPresent) adbExecutable.get().asFile.absolutePath else "adb"
        val serial = resolveSerial(adb)

        fun adbCmd(vararg args: String) = buildList<String> {
            add(adb)
            if (serial != null) {
                add("-s")
                add(serial)
            }
            addAll(args)
        }

        val all: List<MockFileEntry> =
            buildList {
                for (f in mockFiles.files) {
                    if (f.isFile) {
                        val t = f.readText(Charsets.UTF_8)
                        if (t.isNotBlank()) addAll(parse(t, f.name))
                    }
                }
            }
        if (all.isEmpty()) {
            logger.warn("No mocks parsed. Skipping.")
            return
        }

        val payload = json.encodeToString(MultipleMockEntries(entries = all))
        val remote = "/data/local/tmp/mockcat_import_data.json"
        val tmp = File.createTempFile("mockcat", ".json")
        tmp.writeText(payload, Charsets.UTF_8)
        try {
            execOperations.exec { spec ->
                spec.commandLine = adbCmd("push", tmp.absolutePath, remote)
            }
            execOperations.exec { spec ->
                spec.commandLine = adbCmd(
                    "shell", "am", "broadcast",
                    "-a", broadcastAction.get(),
                    "-p", applicationId.get(),
                    "--es", "mock_file_path", remote,
                    "--include-stopped-packages",
                )
            }
        } finally {
            tmp.delete()
            execOperations.exec { spec ->
                spec.commandLine = adbCmd("shell", "rm", remote)
                spec.isIgnoreExitValue = true
            }
        }
    }

    /**
     * Returns the ADB serial to target, or null (meaning let ADB use its default).
     * - If [deviceSerial] is set explicitly: use it.
     * - If exactly one device is connected: auto-select and log it.
     * - If multiple devices are connected: throw with a clear message listing available serials.
     * - If no devices are connected: throw with a clear message.
     */
    private fun resolveSerial(adb: String): String? {
        if (deviceSerial.isPresent) return deviceSerial.get()

        val devices = listConnectedDevices(adb)
        return when (devices.size) {
            0 -> throw GradleException(
                "No devices or emulators connected. " +
                    "Connect a device or start an emulator, then retry.",
            )
            1 -> {
                logger.lifecycle("Target device: ${devices[0]}")
                devices[0]
            }
            else -> promptDeviceSelection(devices)
        }
    }

    private fun listConnectedDevices(adb: String): List<String> {
        val out = ByteArrayOutputStream()
        runCatching {
            execOperations.exec { spec ->
                spec.commandLine = listOf(adb, "devices")
                spec.standardOutput = out
                spec.isIgnoreExitValue = true
            }
        }
        return out.toString(Charsets.UTF_8.name())
            .lineSequence()
            .drop(1) // skip the "List of devices attached" header
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val tab = line.indexOf('\t')
                if (tab == -1) return@mapNotNull null
                if (line.substring(tab + 1).trim() == "device") line.substring(0, tab).trim() else null
            }
            .toList()
    }

    private fun promptDeviceSelection(devices: List<String>): String = throw GradleException(
        "Multiple devices connected. Specify a target device:\n" +
            "  ANDROID_SERIAL=<serial> ./gradlew mockcatImport\n" +
            "  tasks.named<MockcatImportTask>(\"mockcatImport\") { deviceSerial.set(\"<serial>\") }\n" +
            "Connected devices:\n" +
            devices.joinToString("\n") { "  $it" },
    )

    @Suppress("TooGenericExceptionCaught")
    private fun parse(
        text: String,
        fileName: String,
    ): List<MockFileEntry> = try {
        json.decodeFromString<MultipleMockEntries>(text).entries
    } catch (e1: Exception) {
        try {
            listOf(json.decodeFromString<MockFileEntry>(text))
        } catch (e2: Exception) {
            logger.error("Not valid mock file: $fileName: ${e1.message} / ${e2.message}")
            emptyList()
        }
    }
}
