package io.github.xpler2.plugin.compiler.cache

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.gradle.api.file.Directory

@Serializable
data class XplerInitializeCache(
    val sourcePath: String,
    val initializeBean: XplerInitializeBean,
) {
    companion object {
        const val NAME = "xpler_initialize_cache.json"

        fun cache(cacheDirectory: Directory): XplerInitializeCache? {
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