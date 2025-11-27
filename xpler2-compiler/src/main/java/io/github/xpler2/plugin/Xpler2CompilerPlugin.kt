package io.github.xpler2.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.TransformClassesWithAsmTask
import io.github.xpler2.plugin.asm.ASMVisitorFactory
import io.github.xpler2.plugin.asm.generate.HookerEntitiesGenerate
import io.github.xpler2.plugin.asm.generate.LsposedInitGenerate
import io.github.xpler2.plugin.asm.generate.XposedInitGenerate
import io.github.xpler2.plugin.compiler.cache.XplerGenerateCache
import io.github.xpler2.plugin.compiler.task.Xpler2CompilerTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import java.io.File

// `.aar` file structure reference: https://developer.android.com/studio/projects/android-library?hl=zh-cn#aar-contents
class Xpler2CompilerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(AppPlugin::class.java))
            throw RuntimeException("`xpler2-compiler` is only allowed to be applied in the Application module.")

        // compiler task
        val sourceFiles = target.fileTree("src/main") { configTree ->
            configTree.include("**/*.kt", "**/*.java")
            configTree.exclude("**/build/**", "**/generated/**")
        }

        // compiler output directory
        val compilerOutputDirectory = target.layout
            .buildDirectory
            .dir("generated/xpler2")
            .get()

        // compiler core directory
        val coreDirectory = compilerOutputDirectory
            .dir("core")
            .also { it.asFile.mkdirs() }

        // compiler cache directory
        val cacheDirectory = compilerOutputDirectory
            .dir("cache")
            .also { it.asFile.mkdirs() }

        // task registration
        registerCompilerTask(
            target,
            sourceFiles,
            coreDirectory,
            cacheDirectory,
        )

        // asm transform
        val applicationExtension = target.extensions.getByType(ApplicationExtension::class.java)
        val androidComponents = target.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val variantName = variant.name
            val applicationId = applicationExtension.defaultConfig.applicationId

            variant.instrumentation.apply {
                transformClassesWith(
                    ASMVisitorFactory::class.java,
                    InstrumentationScope.ALL,
                ) { params ->
                    params.cacheDirectory = cacheDirectory
                    params.applicationId = applicationId
                    params.variant = variantName
                }
            }
        }
    }

    private fun registerCompilerTask(
        target: Project,
        sourceFiles: ConfigurableFileTree,
        coreDirectory: Directory,
        cacheDirectory: Directory,
    ) {
        // Generate necessary classes after ASM transform is completed
        val generates = setOf(
            XposedInitGenerate,
            LsposedInitGenerate,
            HookerEntitiesGenerate,
        ).onEach { generate ->
            generate.reset()
        }
        target.tasks.withType(TransformClassesWithAsmTask::class.java) { task ->
            task.doFirst {
                XplerGenerateCache.cache(cacheDirectory)?.generates?.forEach { path ->
                    // println("XplerGenerate: remove cache $path")
                    File(path).delete()
                }
            }
            task.doLast {
                val outputDir = task.classesOutputDir.get().asFile
                val results = mutableSetOf<String>()
                generates.forEach { generate ->
                    generate.finish(outputDir)
                        ?.let { path -> results.add(path) }
                }
                // println("XplerGenerate: into cache $results")
                XplerGenerateCache(results).into(cacheDirectory)
            }
        }

        // Configure compiler task
        val compilerTask = target.tasks.register(
            "xpler2Compile",
            Xpler2CompilerTask::class.java,
        ) { task ->
            task.sourceFiles = sourceFiles
            task.coreDirectory = coreDirectory
            task.cacheDirectory = cacheDirectory
        }
        target.tasks.named("preBuild") { task ->
            task.dependsOn(compilerTask)
        }

        // Dependencies
        target.dependencies.add(
            "implementation",
            target.fileTree(
                mapOf(
                    "dir" to "$coreDirectory",
                    "include" to listOf("*.aar")
                )
            )
        )
    }
}