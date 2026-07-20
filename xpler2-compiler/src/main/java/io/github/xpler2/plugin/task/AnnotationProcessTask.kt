package io.github.xpler2.plugin.task

import io.github.xpler2.plugin.parser.XplerEntryResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Scans application sources and replaces generated entry metadata")
abstract class AnnotationProcessTask : DefaultTask() {
    private val json = Json { prettyPrint = false }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    lateinit var sourceFiles: ConfigurableFileTree

    @get:Input
    abstract val generatedEntryClassName: Property<String>

    @get:OutputFile
    abstract val entryOutput: RegularFileProperty

    @TaskAction
    fun action() {
        scanAndGenerateEntry()
    }

    private fun scanAndGenerateEntry() {
        val entry = XplerEntryResolver.resolve(sourceFiles.files, generatedEntryClassName.get())

        // write entry.json
        val entryJson = json.encodeToString(entry)
        val outputFile = entryOutput.get().asFile
        clearLegacyCoreDirectory(outputFile)
        outputFile.parentFile.mkdirs()
        outputFile.writeText(entryJson)

        println(entryJson)
    }

    private fun clearLegacyCoreDirectory(outputFile: java.io.File) {
        outputFile.parentFile.resolve(LEGACY_CORE_DIRECTORY_NAME).deleteRecursively()
    }

    companion object {
        private const val LEGACY_CORE_DIRECTORY_NAME = "core"
    }
}
