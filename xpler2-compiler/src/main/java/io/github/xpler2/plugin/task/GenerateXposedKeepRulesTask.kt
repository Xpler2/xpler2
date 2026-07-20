package io.github.xpler2.plugin.task

import io.github.xpler2.plugin.bean.XplerEntryBean
import io.github.xpler2.plugin.config.XposedKeepRules
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Generates variant-specific keep rules from entry metadata")
abstract class GenerateXposedKeepRulesTask : DefaultTask() {
    private val json = Json { ignoreUnknownKeys = true }

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val entryFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun action() {
        val rules = loadEntry()
            ?.takeIf { it.xposedHint != null }
            ?.let(XposedKeepRules::create)
            .orEmpty()

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(rules)
    }

    private fun loadEntry(): XplerEntryBean? {
        val inputFile = entryFile.asFile.orNull ?: return null
        if (!inputFile.exists()) return null
        return json.decodeFromString(inputFile.readText())
    }
}
