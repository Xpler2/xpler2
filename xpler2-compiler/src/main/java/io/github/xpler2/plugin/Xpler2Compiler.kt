package io.github.xpler2.plugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import io.github.xpler2.plugin.parser.XplerEntryResolver
import io.github.xpler2.plugin.config.XposedConfig
import io.github.xpler2.plugin.config.XposedKeepRules
import io.github.xpler2.plugin.task.AnnotationProcessTask
import io.github.xpler2.plugin.task.ExtractBundledLibrariesTask
import io.github.xpler2.plugin.task.GenerateXplerConfigTask
import io.github.xpler2.plugin.task.GenerateXposedKeepRulesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.TaskProvider

class Xpler2Compiler : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(AppPlugin::class.java))
            throw RuntimeException("`xpler2-compiler` is only allowed to be applied in the Application module.")

        val androidComponents = target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
        val sourceFiles = createSourceFiles(target)
        val generatedEntryClassName = XposedConfig.createGeneratedEntryClassName()
        val xposedOutputPaths = XposedConfig.outputPaths
        val extractBundledXposedApiTask = registerExtractBundledXposedApiTask(target)

        registerBundledXposedApiDependency(
            target = target,
            sourceFiles = sourceFiles,
            extractTask = extractBundledXposedApiTask,
            generatedEntryClassName = generatedEntryClassName,
        )

        androidComponents.onVariants { variant ->
            val variantCapitalized = variant.name.replaceFirstChar { it.uppercase() }

            val buildDir = target.layout.buildDirectory.dir("$BUILD_DIR_NAME/${variant.name}").get()
            val entryFile = buildDir.file("entry.json")
            val xposedKeepRulesFile = buildDir.file(XposedKeepRules.OUTPUT_PATH)

            val scanTask = target.tasks.register(
                "xpler2Scan$variantCapitalized",
                AnnotationProcessTask::class.java
            ) { task ->
                task.sourceFiles = sourceFiles
                task.generatedEntryClassName.set(generatedEntryClassName)
                task.entryOutput.set(entryFile)
            }

            val xposedKeepRulesTask = target.tasks.register(
                "xpler2GenerateXposedKeepRules$variantCapitalized",
                GenerateXposedKeepRulesTask::class.java
            ) { task ->
                task.entryFile.set(entryFile)
                task.outputFile.set(xposedKeepRulesFile)
                task.dependsOn(scanTask)
            }

            val generateTask = target.tasks.register(
                "xpler2GenerateConfig$variantCapitalized",
                GenerateXplerConfigTask::class.java
            ) { task ->
                task.entryFile.set(entryFile)
                task.moduleApplicationId.set(variant.applicationId)
                task.configDirectory.set(buildDir)
                task.sourceOutput.set(buildDir.dir(xposedOutputPaths.sourceOutputPath))
                task.assetsOutput.set(buildDir.dir(xposedOutputPaths.assetsOutputPath))
                task.resOutput.set(buildDir.dir(xposedOutputPaths.resOutputPath))
                task.manifestOutput.set(buildDir.file(xposedOutputPaths.manifestOutputPath))
                task.dependsOn(scanTask)
            }
            variant.sources.java?.addGeneratedSourceDirectory(generateTask, GenerateXplerConfigTask::sourceOutput)
            variant.sources.assets?.addGeneratedSourceDirectory(generateTask, GenerateXplerConfigTask::assetsOutput)
            variant.sources.manifests.addGeneratedManifestFile(generateTask, GenerateXplerConfigTask::manifestOutput)
            variant.sources.res?.addGeneratedSourceDirectory(generateTask, GenerateXplerConfigTask::resOutput)
            if (variant.isMinifyEnabled) {
                variant.proguardFiles.add(xposedKeepRulesTask.flatMap(GenerateXposedKeepRulesTask::outputFile))
            }
        }
    }

    private fun createSourceFiles(target: Project): ConfigurableFileTree {
        return target.fileTree(SOURCE_DIR) { configTree ->
            configTree.include("**/*.kt")
            configTree.exclude("**/build/**", "**/generated/**")
        }
    }

    private fun registerBundledXposedApiDependency(
        target: Project,
        sourceFiles: ConfigurableFileTree,
        extractTask: TaskProvider<ExtractBundledLibrariesTask>,
        generatedEntryClassName: String,
    ) {
        var isBundledDependencyRegistered = false

        target.configurations.named(COMPILE_ONLY_CONFIGURATION_NAME).configure { configuration ->
            configuration.withDependencies { dependencies ->
                if (isBundledDependencyRegistered) return@withDependencies
                if (dependencies.any(::isXposedApiDependency)) return@withDependencies

                val entry = XplerEntryResolver.resolve(sourceFiles.files, generatedEntryClassName)
                if (entry.xposedHint == null) return@withDependencies

                val bundledLibraries = createBundledXposedLibraryFiles(target, extractTask)
                dependencies.add(target.dependencies.create(target.files(bundledLibraries)))
                isBundledDependencyRegistered = true
            }
        }
    }

    private fun registerExtractBundledXposedApiTask(target: Project): TaskProvider<ExtractBundledLibrariesTask> {
        return target.tasks.register(
            EXTRACT_BUNDLED_XPOSED_API_TASK_NAME,
            ExtractBundledLibrariesTask::class.java
        ) { task ->
            task.resourcePaths.set(XposedConfig.bundledCompileOnlyResourcePaths)
            task.resourceHashes.set(
                XposedConfig.bundledCompileOnlyResourcePaths.associateWith(BundledLibraryExtractor::resourceHash)
            )
            task.outputDirectory.set(target.layout.buildDirectory.dir("$BUILD_DIR_NAME/$LIBS_DIR_NAME"))
        }
    }

    private fun createBundledXposedLibraryFiles(
        target: Project,
        extractTask: TaskProvider<ExtractBundledLibrariesTask>,
    ): ConfigurableFileCollection {
        return target.objects.fileCollection()
            .from(
                extractTask
                    .flatMap { it.outputDirectory }
                    .map { directory ->
                        directory.asFileTree.matching { patternFilterable ->
                            patternFilterable.include("*.jar", "*.aar")
                        }
                    }
            )
            .builtBy(extractTask)
    }

    private fun isXposedApiDependency(dependency: Dependency): Boolean {
        return dependency.group == XPOSED_API_GROUP && dependency.name == XPOSED_API_NAME
    }

    companion object {
        private const val COMPILE_ONLY_CONFIGURATION_NAME = "compileOnly"
        private const val EXTRACT_BUNDLED_XPOSED_API_TASK_NAME = "xpler2ExtractBundledXposedApi"
        private const val XPOSED_API_GROUP = "de.robv.android.xposed"
        private const val XPOSED_API_NAME = "api"
        private const val SOURCE_DIR = "src/main"
        private const val BUILD_DIR_NAME = "xpler2"
        private const val LIBS_DIR_NAME = "libs"
    }
}
