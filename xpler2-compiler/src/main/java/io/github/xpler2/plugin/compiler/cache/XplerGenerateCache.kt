package io.github.xpler2.plugin.compiler.cache

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.file.Directory

@Serializable
data class XplerGenerateCache(
    val generates: Set<String>,
) {
    companion object {
        const val NAME = "xpler_generates.json"

        fun cache(cacheDirectory: Directory): XplerGenerateCache? {
            val cacheFile = cacheDirectory.file(NAME).asFile
                .also { it.parentFile.mkdirs() }
            if (!cacheFile.canRead()) return null
            return Json.Default.decodeFromString(cacheFile.readText())
        }
    }

    fun into(cacheDirectory: Directory) {
        val cacheFile = cacheDirectory.file(NAME).asFile
            .also { it.parentFile.mkdirs() }
        cacheFile.writeText(toJson())
    }

    fun toJson(): String {
        return Json.Default.encodeToString(this)
    }
}