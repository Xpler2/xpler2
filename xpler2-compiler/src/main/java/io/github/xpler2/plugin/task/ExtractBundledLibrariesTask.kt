package io.github.xpler2.plugin.task

import io.github.xpler2.plugin.BundledLibraryExtractor
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Extracts libraries bundled inside the plugin artifact")
abstract class ExtractBundledLibrariesTask : DefaultTask() {
    @get:Input
    abstract val resourcePaths: ListProperty<String>

    @get:Input
    abstract val resourceHashes: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun action() {
        BundledLibraryExtractor.syncResources(
            resourcePaths = resourcePaths.get(),
            outputDirectory = outputDirectory.get().asFile,
        )
    }
}
