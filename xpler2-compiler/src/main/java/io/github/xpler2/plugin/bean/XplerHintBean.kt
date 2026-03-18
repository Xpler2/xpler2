package io.github.xpler2.plugin.bean

import kotlinx.serialization.Serializable

@Serializable
data class XplerHintBean(
    val name: String,
    val description: String,
    val scope: List<String>,
) {
    fun resolveName(generatedEntryClassName: String): String {
        if (name.isNotBlank()) {
            return name.trim().also(XplerHintInfo::validateResolvedName)
        }

        return generatedEntryClassName
    }
}
