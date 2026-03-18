package io.github.xpler2.plugin.parser

import io.github.xpler2.plugin.bean.XplerEntryBean
import java.io.File

object XplerEntryResolver {
    fun resolve(sourceFiles: Iterable<File>, generatedEntryClassName: String): XplerEntryBean {
        var resolvedEntry: XplerEntryBean? = null
        var resolvedSourceFile: File? = null

        sourceFiles.forEach { sourceFile ->
            if (!isKotlinSourceFile(sourceFile)) return@forEach

            val entries = XplerAnnotationParser.parser(sourceFile, generatedEntryClassName)
            if (entries.isEmpty()) return@forEach

            if (entries.size > 1) {
                throw IllegalArgumentException("Only one `@XplerHint` is allowed in `${sourceFile.path}`.")
            }

            if (resolvedEntry != null) {
                throw IllegalArgumentException("Only one `@XplerHint` is allowed. Found entries in `${resolvedSourceFile!!.path}` and `${sourceFile.path}`.")
            }

            resolvedEntry = entries.single()
            resolvedSourceFile = sourceFile
        }

        return resolvedEntry
            ?: throw IllegalArgumentException("You must provide a `@XplerHint` annotation. see: https://github.com/Xpler2/xpler2/blob/master/app/src/main/java/io/github/xpler.example/module/Init.kt")
    }

    private fun isKotlinSourceFile(sourceFile: File): Boolean {
        return sourceFile.exists() && sourceFile.isFile && sourceFile.extension == "kt"
    }
}
