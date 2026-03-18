package io.github.xpler2.plugin.util

import java.security.SecureRandom

object RandomIdentifierGenerator {
    fun createGeneratedEntryClassName(
        group: String,
        minLength: Int = DEFAULT_MIN_LENGTH,
        maxLength: Int = DEFAULT_MAX_LENGTH,
    ): String {
        validateParameters(group, minLength, maxLength)

        val effectiveMinLength = maxOf(minLength, minimumLengthForGroup(group))
        val length = effectiveMinLength + secureRandom.nextInt(maxLength - effectiveMinLength + 1)
        val startIndex = createGroupStartIndex(group, length)
        val builder = StringBuilder(length)

        repeat(length) { index ->
            val char = when {
                index in startIndex until (startIndex + group.length) -> group[index - startIndex]
                index == 0 -> FIRST_IDENTIFIER_CHAR_POOL[secureRandom.nextInt(FIRST_IDENTIFIER_CHAR_POOL.size)]
                else -> IDENTIFIER_CHAR_POOL[secureRandom.nextInt(IDENTIFIER_CHAR_POOL.size)]
            }
            builder.append(char)
        }

        return builder.toString()
    }

    private fun validateParameters(group: String, minLength: Int, maxLength: Int) {
        require(group.isNotBlank()) { "group must not be blank." }
        require(minLength > 0) { "minLength must be greater than 0." }
        require(maxLength >= minLength) { "maxLength must be greater than or equal to minLength." }
        require(group.all { it in IDENTIFIER_CHAR_POOL }) {
            "group must contain only letters, digits, or underscores."
        }
        require(maxLength >= minimumLengthForGroup(group)) {
            "maxLength must be at least ${minimumLengthForGroup(group)} to place group `$group` in a valid identifier."
        }
    }

    private fun minimumLengthForGroup(group: String): Int {
        return if (group.first() in FIRST_IDENTIFIER_CHAR_POOL) {
            group.length
        } else {
            group.length + 1
        }
    }

    private fun createGroupStartIndex(group: String, length: Int): Int {
        val maxStartIndex = length - group.length
        if (maxStartIndex == 0) {
            require(group.first() in FIRST_IDENTIFIER_CHAR_POOL) {
                "group must start with a letter when it occupies the full identifier."
            }
            return 0
        }

        val minStartIndex = if (group.first() in FIRST_IDENTIFIER_CHAR_POOL) 0 else 1
        return minStartIndex + secureRandom.nextInt(maxStartIndex - minStartIndex + 1)
    }

    private const val DEFAULT_MIN_LENGTH = 5
    private const val DEFAULT_MAX_LENGTH = 10
    private val secureRandom = SecureRandom()
    private val FIRST_IDENTIFIER_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    private val IDENTIFIER_CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".toCharArray()
}
