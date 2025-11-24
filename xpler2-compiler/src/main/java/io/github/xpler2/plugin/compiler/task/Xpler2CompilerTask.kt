package io.github.xpler2.plugin.compiler.task

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import io.github.xpler2.plugin.compiler.config.LsposedConfig
import io.github.xpler2.plugin.compiler.config.XposedConfig
import io.github.xpler2.plugin.compiler.parser.XplerInitializeParser
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class Xpler2CompilerTask : DefaultTask() {
    private val xplerInitializeBeanMaps = mutableMapOf<File, List<XplerInitializeBean?>>()

    @get:InputFiles
    lateinit var sourceFiles: ConfigurableFileTree

    @get:OutputDirectory
    lateinit var coreDirectory: Directory

    @get:OutputDirectory
    lateinit var cacheDirectory: Directory

    @TaskAction
    fun compiler() {
        // scan all `.java` and `.kt` source files
        sourceFiles.forEach { sourceFile ->
            if (!sourceFile.exists()) return@forEach
            if (!(sourceFile.isFile && (sourceFile.extension == "kt" || sourceFile.extension == "java"))) return@forEach // skip non source code files
            val parse = XplerInitializeParser.parse(sourceFile)
            if (parse.isEmpty()) return@forEach // skip empty
            xplerInitializeBeanMaps[sourceFile] = parse
        }

        // ensure the uniqueness of `@XplerInitialize` annotations
        if (xplerInitializeBeanMaps.isEmpty())
            throw RuntimeException("You must provide a `@XplerInitialize` annotation, otherwise you should not use this plugin.")

        if (xplerInitializeBeanMaps.size > 1)
            throw RuntimeException("Only one initialization entry is allowed in an Xposed project, please remove the redundant `@XplerInitialize` annotation.")

        val xplerInitializeBeans = xplerInitializeBeanMaps.entries.single()
        if (xplerInitializeBeans.value.size > 1)
            throw RuntimeException("Only one initialization entry is allowed in an Xposed project, please remove the redundant `@XplerInitialize` annotation.")

        val (sourceFile, beans) = xplerInitializeBeanMaps.entries.single()
        val xplerInitializeBean = beans.singleOrNull()
            ?: throw RuntimeException("Only one initialization entry is allowed in an Xposed project, please remove the redundant `@XplerInitialize` annotation.")

        // result caching
        XplerInitializeCache(sourceFile.absolutePath, xplerInitializeBean)
            .into(cacheDirectory)
        println("XplerInitialize: into cache $sourceFile -> ${Json.encodeToString(xplerInitializeBean)}")

        // build Xposed/Lspose configuration information
        XposedConfig.init(coreDirectory, xplerInitializeBean)
        LsposedConfig.init(coreDirectory, xplerInitializeBean)
    }
}