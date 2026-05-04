package com.mockcat.gradle

import com.mockcat.api.MockFileEntry
import com.mockcat.api.MultipleMockEntries
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class MockcatImportTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputFile
    abstract val adbExecutable: Property<File>

    @get:InputFiles
    abstract val mockFiles: ConfigurableFileCollection

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    var broadcastAction: String = "com.mockcat.action.IMPORT_MOCKS"

    private val json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }

    @TaskAction
    fun importRun() {
        val adb = adbExecutable.get().absolutePath
        val all: List<MockFileEntry> =
            buildList {
                for (f in mockFiles.files) {
                    if (f.isFile) {
                        val t = f.readText(Charsets.UTF_8)
                        if (t.isNotBlank()) {
                            addAll(parse(t, f.name))
                        }
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
            execOperations.exec {
                commandLine(adb, "push", tmp.absolutePath, remote)
            }
            val cmd =
                buildList {
                    add(adb)
                    add("shell")
                    add("am")
                    add("broadcast")
                    add("-a")
                    add(broadcastAction)
                    add("-p")
                    add(applicationId.get())
                    add("--es")
                    add("mock_file_path")
                    add(remote)
                    add("--include-stopped-packages")
                }
            execOperations.exec { commandLine(cmd) }
        } finally {
            tmp.delete()
            execOperations.exec {
                commandLine(adb, "shell", "rm", remote)
                isIgnoreExitValue = true
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun parse(
        text: String,
        fileName: String,
    ): List<MockFileEntry> {
        return try {
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
}
