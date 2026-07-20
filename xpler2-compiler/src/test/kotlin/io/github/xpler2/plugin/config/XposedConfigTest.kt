package io.github.xpler2.plugin.config

import io.github.xpler2.plugin.bean.FunctionInfo
import io.github.xpler2.plugin.bean.XplerEntryBean
import io.github.xpler2.plugin.bean.XplerHintInfo
import io.github.xpler2.plugin.bean.XposedHintInfo
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class XposedConfigTest {
    @Test
    fun `generated sources reference the published xposed package`() {
        val root = createTempDirectory("xpler2-config-test").toFile()
        try {
            val outputs = XplerConfigOutputs(
                outputDirectory = root.resolve("xposed"),
                sourceOutputDirectory = root.resolve("xposed/source"),
                assetsOutputDirectory = root.resolve("xposed/assets"),
                resOutputDirectory = root.resolve("xposed/res"),
                manifestOutputFile = root.resolve("xposed/manifest/AndroidManifest.xml"),
            )
            val entry = XplerEntryBean(
                function = FunctionInfo(
                    packageName = "example.module",
                    functionName = "init",
                    parameterName = "module",
                    parameterType = "XplerModuleInterface",
                    isTopLevel = true,
                ),
                xplerHint = XplerHintInfo(
                    name = "",
                    resolvedName = "GeneratedEntry",
                    description = "Example",
                    scope = listOf("example.target"),
                ),
                xposedHint = XposedHintInfo(version = 82),
            )

            XposedConfig.generate(
                XplerConfigContext(
                    outputs = outputs,
                    entry = entry,
                    moduleApplicationId = "example.module",
                )
            )

            val entrySource = outputs.sourceOutputDirectory.resolve("GeneratedEntry.kt").readText()
            val statusSource = outputs.sourceOutputDirectory.resolve("XplerStatusProvider.kt").readText()
            assertContains(entrySource, "import io.github.xpler2.xposed.XposedModule")
            assertContains(statusSource, "import io.github.xpler2.xposed.XposedStatus")
            assertFalse(entrySource.contains("io.xpler2.github"))
            assertFalse(statusSource.contains("io.xpler2.github"))
        } finally {
            root.deleteRecursively()
        }
    }
}
