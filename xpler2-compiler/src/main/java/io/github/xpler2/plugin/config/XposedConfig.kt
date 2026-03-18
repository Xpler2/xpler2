package io.github.xpler2.plugin.config

import io.github.xpler2.plugin.bean.XplerEntryBean
import io.github.xpler2.plugin.util.RandomIdentifierGenerator
import java.io.File

object XposedConfig : XplerConfig {
    override val outputPaths = XplerConfigOutputPaths(
        outputDirectoryPath = OUTPUT_DIRECTORY_NAME,
        sourceOutputPath = "$OUTPUT_DIRECTORY_NAME/source",
        assetsOutputPath = "$OUTPUT_DIRECTORY_NAME/assets",
        resOutputPath = "$OUTPUT_DIRECTORY_NAME/res",
        manifestOutputPath = "$OUTPUT_DIRECTORY_NAME/manifest/AndroidManifest.xml",
    )
    val bundledCompileOnlyResourcePaths = listOf("$RESOURCE_DIRECTORY/api-82.jar")

    fun createGeneratedEntryClassName(group: String = DEFAULT_ENTRY_CLASS_NAME_GROUP): String {
        return RandomIdentifierGenerator.createGeneratedEntryClassName(group)
    }

    private const val XPOSED_MODULE_CLASS_NAME = "io.xpler2.github.xposed.XposedModule"
    private const val XPOSED_INIT_FILE = "xposed_init"

    override fun generate(context: XplerConfigContext) {
        val xposedHint = context.entry.xposedHint
        if (xposedHint == null) {
            clearOutputDirectory(context.outputs.outputDirectory)
            return
        }

        prepareOutputDirectories(context.outputs)

        generateSource(
            sourceOutputDirectory = context.outputs.sourceOutputDirectory,
            entry = context.entry,
            moduleApplicationId = context.moduleApplicationId,
        )
        generateStatusProvider(context.outputs.sourceOutputDirectory)
        generateAssets(context.outputs.assetsOutputDirectory, context.entry)
        generateRes(context.outputs.resOutputDirectory, context.entry)
        generateManifest(context.outputs.manifestOutputFile, xposedHint.version)
    }

    private fun generateSource(sourceOutputDirectory: File, entry: XplerEntryBean, moduleApplicationId: String) {
        val entryPackageName = entry.xplerHint.resolvedPackageName
        val entryClassName = entry.xplerHint.resolvedClassName
        val sourceDirectory = entryPackageName
            ?.let { sourceOutputDirectory.resolve(it.toPackagePath()) }
            ?: sourceOutputDirectory
        sourceDirectory.mkdirs()

        val code = buildString {
            if (entryPackageName != null) {
                appendLine("package $entryPackageName")
                appendLine()
            }
            appendLine("import ${entry.function.packageName}.${entry.function.functionName}")
            appendLine("import de.robv.android.xposed.callbacks.XC_LoadPackage")
            appendLine("import $XPOSED_MODULE_CLASS_NAME")
            appendLine()
            appendLine("class $entryClassName : XposedModule() {")
            appendLine("    override val moduleApplicationId: String = \"$moduleApplicationId\"")
            appendLine()
            appendLine("    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {")
            appendLine("        super.handleLoadPackage(lpparam)")
            appendLine("        ${entry.function.functionName}(this)")
            appendLine("    }")
            appendLine("}")
        }

        sourceDirectory.resolve("$entryClassName.kt").writeText(code)
    }

    private fun generateStatusProvider(sourceOutputDirectory: File) {
        sourceOutputDirectory.mkdirs()

        val code = """
import io.github.xpler2.XplerModuleStatus
import io.xpler2.github.xposed.XposedStatus

object ${XposedKeepRules.STATUS_PROVIDER_CLASS_NAME} : XplerModuleStatus {
    private val delegates = listOf<XplerModuleStatus>(
        // LsposedStatus,
        XposedStatus,
    )

    private val activeStatus: XplerModuleStatus
        get() = delegates.firstOrNull { it.isActivate } ?: XplerModuleStatus.empty

    override val isActivate: Boolean
        get() = activeStatus.isActivate

    override val apiVersion: Int
        get() = activeStatus.apiVersion

    override val frameworkName: String
        get() = activeStatus.frameworkName

    override val frameworkVersion: String
        get() = activeStatus.frameworkVersion

    override val frameworkVersionCode: Long
        get() = activeStatus.frameworkVersionCode
}
""".trimIndent()

        sourceOutputDirectory.resolve("${XposedKeepRules.STATUS_PROVIDER_CLASS_NAME}.kt").writeText(code)
    }

    private fun generateAssets(assetsOutputDirectory: File, entry: XplerEntryBean) {
        assetsOutputDirectory.mkdirs()
        val entryPackageName = entry.xplerHint.resolvedPackageName
        val entryClassName = entry.xplerHint.resolvedClassName
        val xposedInitClassName = entryPackageName
            ?.let { "$it.$entryClassName" }
            ?: entryClassName

        assetsOutputDirectory.resolve(XPOSED_INIT_FILE).writeText(xposedInitClassName)
    }

    private fun generateRes(resOutputDirectory: File, entry: XplerEntryBean) {
        val resDirectory = resOutputDirectory.resolve("values")
        resDirectory.mkdirs()

        val xml = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array name="xposed_scope">
${entry.xplerHint.scope.joinToString("\n") { "        <item>${it.escapeXml()}</item>" }}
    </string-array>
    <string name="xposed_description">${entry.xplerHint.description.escapeXml()}</string>
</resources>
""".trimIndent()

        resDirectory.resolve("values.xml").writeText(xml)
    }

    private fun generateManifest(manifestFile: File, xposedMinVersion: Int) {
        manifestFile.parentFile.mkdirs()

        val xml = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
        <meta-data
            android:name="xposedmodule"
            android:value="true" />
        <meta-data
            android:name="xposedscope"
            android:resource="@array/xposed_scope" />
        <meta-data
            android:name="xposeddescription"
            android:value="@string/xposed_description" />
        <meta-data
            android:name="xposedminversion"
            android:value="$xposedMinVersion" />
    </application>
</manifest>
""".trimIndent()

        manifestFile.writeText(xml)
    }

    private fun clearOutputDirectory(outputDirectory: File) {
        outputDirectory.deleteRecursively()
    }

    private fun prepareOutputDirectories(outputs: XplerConfigOutputs) {
        outputs.outputDirectory.mkdirs()
        recreateDirectory(outputs.sourceOutputDirectory)
        recreateDirectory(outputs.assetsOutputDirectory)
        recreateDirectory(outputs.resOutputDirectory)
        recreateDirectory(outputs.manifestOutputFile.parentFile)
    }

    private fun recreateDirectory(directory: File) {
        clearOutputDirectory(directory)
        directory.mkdirs()
    }

    private const val OUTPUT_DIRECTORY_NAME = "xposed"
    private const val RESOURCE_DIRECTORY = "xposed"
    private const val DEFAULT_ENTRY_CLASS_NAME_GROUP = "x"
}

private fun String.toPackagePath(): String = replace('.', '/')

private fun String.escapeXml(): String {
    return buildString(length) {
        for (char in this@escapeXml) {
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(char)
            }
        }
    }
}
