package io.github.xpler2.plugin.compiler.bean

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class XplerInitializeBean(
    val name: String,
    val description: String,
    val scope: List<String>,
    @EncodeDefault val xposed: Boolean = true,
    @EncodeDefault val xposedVersion: Int = 82,
    @EncodeDefault val lsposed: Boolean = true,
    @EncodeDefault val lsposedTargetVersion: Int = 100,
    @EncodeDefault val lsposedMinVersion: Int = 100,
    @EncodeDefault val lsposedStaticScope: Boolean = true,
    @EncodeDefault val lsposedCompatAnnotation: Boolean = true,
    @EncodeDefault val xposedInit: String = randomName("x", name, 5, 12),
    @EncodeDefault val lsposedInit: String = randomName("l", name, 5, 12),
) {
    companion object {
        private const val BODY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        fun randomName(flag: String, name: String, from: Int, until: Int): String {
            val safeFrom = from.coerceAtLeast(1)
            val safeUntil = until.coerceAtLeast(safeFrom + 1)

            val length = Random.nextInt(safeFrom, safeUntil)
            val flagIndex = Random.nextInt(length)

            val baseName = if (name.equals($$"$random$", ignoreCase = true)) "" else name
            val estimatedSize = baseName.length + length + flag.length

            return buildString(estimatedSize) {
                append(baseName)

                for (i in 0 until length) {
                    if (i == flagIndex) {
                        append(flag)
                    } else {
                        append(BODY_CHARS[Random.nextInt(BODY_CHARS.length)])
                    }
                }
            }
        }
    }
}