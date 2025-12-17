package io.github.xpler2.plugin.compiler.parser

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import java.io.File

/**
 * XplerInitialize annotation Parser
 */
object XplerInitializeParser {
    private const val ANNOTATION_START = "@XplerInitialize"
    private val KEY_VALUE_REGEX = """(\w+)\s*=\s*(.*)""".toRegex()
    private val STRING_VALUE_REGEX = """^"([^"]*)"$""".toRegex()
    private val ARRAY_VALUE_REGEX = """^\[([\s\S]*)]$""".toRegex()
    private val ARRAY_ITEM_REGEX = """"([^"]*)"""".toRegex()

    fun parse(file: File): List<XplerInitializeBean> {
        val lines = file.readLines()
        val annotationContents: MutableList<String?> = mutableListOf()

        // find the start line of the annotation
        for ((index, line) in lines.withIndex()) {
            if (line.trim().startsWith(ANNOTATION_START)) {
                annotationContents.add(extractAnnotationContent(lines, index))
            }
        }

        return annotationContents
            .filter { content -> content != null }
            .map { content -> parseAnnotationContent(content!!) }
    }

    private fun extractAnnotationContent(lines: List<String>, startIndex: Int): String? {
        val content = StringBuilder()
        var openBracketCount = 0
        var foundStart = false

        for (i in startIndex until lines.size) {
            val currentLine = lines[i]

            if (!foundStart) {
                val annotationIndex = currentLine.indexOf(ANNOTATION_START)
                if (annotationIndex == -1) continue

                // find the first annotation '('
                val startBracketIndex = currentLine.indexOf('(', annotationIndex)
                if (startBracketIndex == -1) continue

                // start adding content from '('
                val lineFromBracket = currentLine.substring(startBracketIndex)
                content.append(lineFromBracket)
                openBracketCount += lineFromBracket.count { it == '(' }
                openBracketCount -= lineFromBracket.count { it == ')' }
                foundStart = true
            } else {
                content.append(currentLine)
                openBracketCount += currentLine.count { it == '(' }
                openBracketCount -= currentLine.count { it == ')' }
            }

            // if the annotation start is found and the brackets are balanced, the content is returned
            if (foundStart && openBracketCount <= 0) {
                return content.toString()
            }
        }
        return null
    }

    private fun parseAnnotationContent(content: String): XplerInitializeBean {
        // remove the outer parentheses and split the key-value pairs
        val cleanContent = content
            .removeSurrounding("(", ")")
            .split(',')
            .joinToString(separator = ",") { it.trim() }

        val properties = mutableMapOf<String, String>()

        // analyze key value pairs
        var currentKey: String? = null
        val currentValue = StringBuilder()
        var inString = false
        var inArray = 0

        for (char in cleanContent) {
            when {
                char == '"' && !inString -> inString = true
                char == '"' && inString -> inString = false
                char == '[' && !inString -> inArray++
                char == ']' && !inString -> inArray--
                char == ',' && !inString && inArray == 0 -> {
                    currentKey?.let { key ->
                        properties[key] = currentValue.toString().trim()
                    }
                    currentValue.clear()
                    currentKey = null
                    continue
                }
            }

            if (currentKey == null && !inString && inArray == 0 && char != ' ') {
                currentValue.append(char)
                val match = KEY_VALUE_REGEX.find(currentValue.toString())
                if (match != null) {
                    currentKey = match.groupValues[1]
                    currentValue.clear()
                    currentValue.append(match.groupValues[2].trim())
                }
            } else {
                currentValue.append(char)
            }
        }

        // add the last attribute
        currentKey?.let {
            properties[it] = currentValue.toString().trim()
        }

        // parse the properties into XplerInitializeBean
        return XplerInitializeBean(
            name = parseString(properties["name"], ""),
            description = parseString(properties["description"], ""),
            scope = parseArray(properties["scope"], emptyList()),
            xposed = parseBoolean(properties["xposed"], true),
            xposedVersion = parseInt(properties["xposedVersion"], 82),
            lsposed = parseBoolean(properties["lsposed"], true),
            lsposedTargetVersion = parseInt(properties["lsposedTargetVersion"], 100),
            lsposedMinVersion = parseInt(properties["lsposedMinVersion"], 100),
            lsposedStaticScope = parseBoolean(properties["lsposedStaticScope"], true),
            lsposedCompatAnnotation = parseBoolean(properties["lsposedCompatAnnotation"], true)
        )
    }

    private fun parseInt(value: String?, default: Int): Int {
        if (value == null) return default
        return value.toIntOrNull() ?: default
    }

    private fun parseBoolean(value: String?, default: Boolean): Boolean {
        if (value == null) return default
        return value.toBoolean()
    }

    private fun parseString(value: String?, default: String): String {
        if (value == null) return default
        return STRING_VALUE_REGEX.find(value)?.groupValues?.get(1) ?: default
    }

    private fun parseArray(value: String?, default: List<String>): List<String> {
        if (value == null) return default

        // process Java arrays (curly braces) and Kotlin arrays (square braces)
        val arrayContent = when {
            value.startsWith('{') && value.endsWith('}') ->
                value.removeSurrounding("{", "}")

            value.startsWith('[') && value.endsWith(']') ->
                ARRAY_VALUE_REGEX.find(value)?.groupValues?.get(1) ?: ""

            else -> value
        }

        return ARRAY_ITEM_REGEX.findAll(arrayContent)
            .map { it.groupValues[1] }
            .toList()
            .ifEmpty { default }
    }
}