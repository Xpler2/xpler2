package io.github.xpler2.plugin.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class XplerEntryBean(
    val function: FunctionInfo,
    val xplerHint: XplerHintInfo,
    val xposedHint: XposedHintInfo?
)

@Serializable
data class FunctionInfo(
    val packageName: String,
    val functionName: String,
    val parameterName: String,
    val parameterType: String,
    val isTopLevel: Boolean
)

@Serializable
data class XplerHintInfo(
    val name: String,
    val resolvedName: String,
    val description: String,
    val scope: List<String>
) {
    @Transient
    private val parsedResolvedName = parseResolvedName(resolvedName)

    val resolvedPackageName: String?
        get() = parsedResolvedName.packageName

    val resolvedClassName: String
        get() = parsedResolvedName.className

    companion object {
        private val IDENTIFIER_REGEX = """[A-Za-z_][A-Za-z0-9_]*""".toRegex()

        internal fun validateResolvedName(resolvedName: String) {
            parseResolvedName(resolvedName)
        }

        private fun parseResolvedName(resolvedName: String): ResolvedNameInfo {
            val trimmedName = resolvedName.trim()
            val packageName = if (trimmedName.contains('.')) {
                trimmedName.substringBeforeLast('.').trim().also {
                    validatePackageName(resolvedName, it)
                }
            } else {
                null
            }

            val className = trimmedName.substringAfterLast('.').trim()
            validateClassName(resolvedName, className)
            return ResolvedNameInfo(packageName, className)
        }

        private fun validatePackageName(resolvedName: String, packageName: String) {
            if (packageName.isBlank()) {
                throw IllegalArgumentException(
                    "Invalid @XplerHint name `$resolvedName`: package name must not be blank."
                )
            }

            val invalidSegment = packageName
                .split('.')
                .firstOrNull { it.isBlank() || !IDENTIFIER_REGEX.matches(it) }

            if (invalidSegment != null) {
                val message = if (invalidSegment.isBlank()) {
                    "package name contains an empty segment."
                } else {
                    "`$invalidSegment` is not a valid package segment."
                }
                throw IllegalArgumentException("Invalid @XplerHint name `$resolvedName`: $message")
            }
        }

        private fun validateClassName(resolvedName: String, className: String) {
            if (className.isBlank()) {
                throw IllegalArgumentException(
                    "Invalid @XplerHint name `$resolvedName`: class name must not be blank."
                )
            }

            if (!IDENTIFIER_REGEX.matches(className)) {
                throw IllegalArgumentException(
                    "Invalid @XplerHint name `$resolvedName`: `$className` is not a valid Kotlin class name."
                )
            }
        }
    }

    private data class ResolvedNameInfo(
        val packageName: String?,
        val className: String,
    )
}

@Serializable
data class XposedHintInfo(
    val version: Int
)
