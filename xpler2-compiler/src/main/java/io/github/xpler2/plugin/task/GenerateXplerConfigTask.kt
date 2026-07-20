package io.github.xpler2.plugin.task

import io.github.xpler2.plugin.bean.XplerEntryBean
import io.github.xpler2.plugin.config.XplerConfig
import io.github.xpler2.plugin.config.XplerConfigContext
import io.github.xpler2.plugin.config.XplerConfigOutputs
import io.github.xpler2.plugin.config.XplerConfigs
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Recreates variant-specific generated sources and Android resources")
abstract class GenerateXplerConfigTask : DefaultTask() {
    private val json = Json { ignoreUnknownKeys = true }

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val entryFile: RegularFileProperty

    @get:Input
    abstract val moduleApplicationId: Property<String>

    @get:Input
    abstract val xposedRuntimeAvailable: Property<Boolean>

    @get:Internal
    abstract val configDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val sourceOutput: DirectoryProperty

    @get:OutputDirectory
    abstract val assetsOutput: DirectoryProperty

    @get:OutputDirectory
    abstract val resOutput: DirectoryProperty

    @get:OutputFile
    abstract val manifestOutput: RegularFileProperty

    @TaskAction
    fun action() {
        val configRootDirectory = configDirectory.get().asFile
        val entry = loadEntry()
        if (entry == null) {
            clearOutputDirectory(configRootDirectory)
            return
        }
        if (entry.xposedHint != null && !xposedRuntimeAvailable.get()) {
            throw GradleException(
                "`@XposedHint` requires an `io.github.xpler2:xpler2-xposed` dependency."
            )
        }

        XplerConfigs.all.forEach { config ->
            config.generate(createConfigContext(config, entry))
        }
    }

    private fun createConfigContext(config: XplerConfig, entry: XplerEntryBean): XplerConfigContext {
        return XplerConfigContext(
            outputs = XplerConfigOutputs(
                outputDirectory = configDirectory.get().asFile.resolve(config.outputPaths.outputDirectoryPath),
                sourceOutputDirectory = sourceOutput.get().asFile,
                assetsOutputDirectory = assetsOutput.get().asFile,
                resOutputDirectory = resOutput.get().asFile,
                manifestOutputFile = manifestOutput.get().asFile,
            ),
            entry = entry,
            moduleApplicationId = moduleApplicationId.get(),
        )
    }

    private fun loadEntry(): XplerEntryBean? {
        val inputFile = entryFile.asFile.orNull ?: return null
        if (!inputFile.exists()) return null
        return json.decodeFromString(inputFile.readText())
    }

    private fun clearOutputDirectory(outputDirectory: File) {
        outputDirectory.deleteRecursively()
    }
}
