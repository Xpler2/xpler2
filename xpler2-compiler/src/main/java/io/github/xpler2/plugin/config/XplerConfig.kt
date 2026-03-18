package io.github.xpler2.plugin.config

import io.github.xpler2.plugin.bean.XplerEntryBean
import org.gradle.api.file.Directory
import java.io.File

interface XplerConfig {
    val outputPaths: XplerConfigOutputPaths

    fun generate(context: XplerConfigContext)
}

data class XplerConfigOutputPaths(
    val outputDirectoryPath: String,
    val sourceOutputPath: String,
    val assetsOutputPath: String,
    val resOutputPath: String,
    val manifestOutputPath: String,
) {
    fun resolve(rootDirectory: Directory): XplerConfigOutputs {
        val rootFile = rootDirectory.asFile

        return XplerConfigOutputs(
            outputDirectory = rootFile.resolve(outputDirectoryPath),
            sourceOutputDirectory = rootFile.resolve(sourceOutputPath),
            assetsOutputDirectory = rootFile.resolve(assetsOutputPath),
            resOutputDirectory = rootFile.resolve(resOutputPath),
            manifestOutputFile = rootFile.resolve(manifestOutputPath),
        )
    }
}

data class XplerConfigOutputs(
    val outputDirectory: File,
    val sourceOutputDirectory: File,
    val assetsOutputDirectory: File,
    val resOutputDirectory: File,
    val manifestOutputFile: File,
)

data class XplerConfigContext(
    val outputs: XplerConfigOutputs,
    val entry: XplerEntryBean,
    val moduleApplicationId: String,
)

object XplerConfigs {
    val all: List<XplerConfig> = listOf(XposedConfig)
}
