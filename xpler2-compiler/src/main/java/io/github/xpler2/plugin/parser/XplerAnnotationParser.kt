package io.github.xpler2.plugin.parser

import io.github.xpler2.plugin.bean.*
import java.io.File

object XplerAnnotationParser {
    private const val XPLER_HINT = "XplerHint"
    private const val XPOSED_HINT = "XposedHint"
    private const val XPOSED_DEFAULT_VERSION = 82
    private const val KOTLIN_RAW_STRING = "\"\"\""
    private const val FUNCTION_KEYWORD = "fun"
    private const val PRIVATE_MODIFIER = "private"
    private const val SUSPEND_MODIFIER = "suspend"
    private val ARRAY_CALL_REGEX = """(?:arrayOf|listOf)\s*(?:<[^>]+>)?\s*\(([\s\S]*)\)""".toRegex()
    private val ENTRY_PARAMETER_TYPES = setOf(
        "XplerModuleInterface",
        "io.github.xpler2.XplerModuleInterface",
    )

    fun parser(file: File, generatedEntryClassName: String): List<XplerEntryBean> {
        val content = stripComments(file.readText())
        val packageName = extractPackage(content)
        val annotations = collectAnnotations(content)
        if (annotations.isEmpty()) return emptyList()

        val beans = linkedMapOf<Int, MutableXplerEntryBean>()
        annotations.forEach { annotation ->
            val key = annotation.declarationIndex.takeIf { it >= 0 } ?: annotation.startIndex
            val bean = beans.getOrPut(key) { MutableXplerEntryBean() }

            when (annotation.simpleName) {
                XPLER_HINT -> bean.xplerHintBean = parserXplerHint(annotation.arguments)
                XPOSED_HINT -> bean.xposedHintBean = parserXposedHint(annotation.arguments)
            }
        }

        return beans.mapNotNull { (declarationIndex, bean) ->
            val xplerHint = bean.xplerHintBean ?: return@mapNotNull null
            val functionInfo = extractFunctionInfo(file, content, declarationIndex, packageName)
                ?: throw invalidEntryFunction(file, "entry annotation must target a Kotlin top-level function.")
            validateFunctionInfo(file, content, declarationIndex, functionInfo)
            val resolvedName = xplerHint.resolveName(generatedEntryClassName)

            XplerEntryBean(
                function = functionInfo,
                xplerHint = XplerHintInfo(
                    name = xplerHint.name,
                    resolvedName = resolvedName,
                    description = xplerHint.description,
                    scope = xplerHint.scope
                ),
                xposedHint = bean.xposedHintBean?.let { XposedHintInfo(it.version) }
            )
        }.toList()
    }

    private fun extractPackage(content: String): String {
        val packageRegex = """package\s+([\w.]+)""".toRegex()
        return packageRegex.find(content)?.groupValues?.get(1) ?: ""
    }

    private fun extractFunctionInfo(file: File, content: String, startIndex: Int, packageName: String): FunctionInfo? {
        var index = startIndex
        val modifiers = linkedSetOf<String>()

        while (index < content.length) {
            index = skipWhitespace(content, index)
            if (index >= content.length) return null
            if (isFunctionKeyword(content, index)) break
            if (content[index] == '@') {
                throw invalidEntryFunction(file, "entry function must not declare extra annotations between `@XplerHint` and `fun`.")
            }
            if (content[index] == '`') {
                throw invalidEntryFunction(file, "entry function declarations using backtick names are not supported.")
            }
            if (!isIdentifierStart(content[index])) return null

            val modifierStart = index
            index = skipIdentifier(content, index)
            modifiers += content.substring(modifierStart, index)
        }

        if (!isFunctionKeyword(content, index)) return null
        if (PRIVATE_MODIFIER in modifiers) {
            throw invalidEntryFunction(file, "entry function must not be private because generated code calls it from another file.")
        }
        if (SUSPEND_MODIFIER in modifiers) {
            throw invalidEntryFunction(file, "entry function must not be suspend because generated Xposed entry calls it directly.")
        }
        index += FUNCTION_KEYWORD.length

        while (index < content.length && content[index].isWhitespace()) index++
        if (index < content.length && content[index] == '<') {
            throw invalidEntryFunction(file, "generic entry functions are not supported.")
        }
        if (index < content.length && content[index] == '`') {
            throw invalidEntryFunction(file, "entry function declarations using backtick names are not supported.")
        }
        if (index >= content.length || !isIdentifierStart(content[index])) return null

        val nameStart = index
        index = skipIdentifier(content, index)
        val functionName = content.substring(nameStart, index)

        while (index < content.length && content[index].isWhitespace()) index++
        if (index < content.length && content[index] == '.') {
            throw invalidEntryFunction(file, "extension entry functions are not supported.")
        }
        if (index >= content.length || content[index] != '(') return null

        val parameterListStart = index
        val parameterListEnd = findClosingParenthesis(content, parameterListStart)
        if (parameterListEnd == -1) {
            throw invalidEntryFunction(file, "entry function parameter list is not closed.")
        }
        val (parameterName, parameterType) = parseEntryParameter(
            file = file,
            parameterListContent = content.substring(parameterListStart + 1, parameterListEnd),
        )

        return FunctionInfo(
            packageName = packageName,
            functionName = functionName,
            parameterName = parameterName,
            parameterType = parameterType,
            isTopLevel = true
        )
    }

    private fun validateFunctionInfo(
        file: File,
        content: String,
        declarationIndex: Int,
        functionInfo: FunctionInfo,
    ) {
        if (!isTopLevelDeclaration(content, declarationIndex)) {
            throw invalidEntryFunction(file, "entry function must be top-level.")
        }
        if (functionInfo.functionName.isBlank()) {
            throw invalidEntryFunction(file, "entry function name must not be blank.")
        }
        if (functionInfo.parameterType !in ENTRY_PARAMETER_TYPES) {
            throw invalidEntryFunction(
                file,
                "entry function parameter type must be `XplerModuleInterface`."
            )
        }
    }

    private fun parseEntryParameter(file: File, parameterListContent: String): Pair<String, String> {
        val trimmedParameters = parameterListContent.trim()
        if (trimmedParameters.isEmpty()) {
            throw invalidEntryFunction(file, "entry function must declare exactly one parameter of type `XplerModuleInterface`.")
        }

        val parameters = splitTopLevel(trimmedParameters)
        if (parameters.size != 1) {
            throw invalidEntryFunction(file, "entry function must declare exactly one parameter.")
        }

        val parameter = parameters.single().trim()
        if (parameter.startsWith("@")) {
            throw invalidEntryFunction(file, "entry function parameter annotations are not supported.")
        }
        if (findTopLevelEquals(parameter) != -1) {
            throw invalidEntryFunction(file, "entry function parameter default values are not supported.")
        }

        val separatorIndex = findTopLevelSeparator(parameter, ':')
        if (separatorIndex == -1) {
            throw invalidEntryFunction(file, "entry function parameter must declare an explicit type.")
        }

        val parameterName = parameter.substring(0, separatorIndex).trim()
        val parameterType = parameter.substring(separatorIndex + 1).trim().replace(" ", "")

        if (parameterName.contains(' ')) {
            throw invalidEntryFunction(file, "entry function parameter modifiers are not supported.")
        }
        if (!isSimpleIdentifier(parameterName)) {
            throw invalidEntryFunction(file, "entry function parameter name `$parameterName` is not a valid Kotlin identifier.")
        }
        if (parameterType.isEmpty()) {
            throw invalidEntryFunction(file, "entry function parameter type must not be blank.")
        }
        if (parameterType.contains('?')) {
            throw invalidEntryFunction(file, "entry function parameter type must not be nullable.")
        }
        if (parameterType.contains("->") || parameterType.startsWith("(")) {
            throw invalidEntryFunction(file, "function-type parameters are not supported for entry functions.")
        }

        return parameterName to parameterType
    }

    private fun parserXplerHint(content: String?): XplerHintBean? {
        if (content == null) return null

        val arguments = parseArguments(content)
        return XplerHintBean(
            name = parseString(arguments.value("name", 0), ""),
            description = parseString(arguments.value("description", 1), ""),
            scope = parseArray(arguments.value("scope", 2), emptyList()),
        )
    }

    private fun parserXposedHint(content: String?): XposedHintBean {
        val arguments = parseArguments(content)
        return XposedHintBean(
            version = parseInt(arguments.value("version", 0), XPOSED_DEFAULT_VERSION),
        )
    }

    private fun collectAnnotations(content: String): List<AnnotationMatch> {
        val annotations = mutableListOf<AnnotationMatch>()
        var index = 0

        while (index < content.length) {
            when {
                content.startsWith(KOTLIN_RAW_STRING, index) -> {
                    index = skipRawString(content, index)
                }

                content[index] == '"' -> {
                    index = skipQuoted(content, index, '"')
                }

                content[index] == '\'' -> {
                    index = skipQuoted(content, index, '\'')
                }

                content[index] == '@' && isAnnotationStart(content, index) -> {
                    val annotation = readAnnotation(content, index)
                    if (annotation != null) {
                        if (annotation.simpleName == XPLER_HINT || annotation.simpleName == XPOSED_HINT) {
                            annotations += annotation
                        }
                        index = annotation.endIndex
                    } else {
                        index++
                    }
                }

                else -> index++
            }
        }

        return annotations
    }

    private fun readAnnotation(content: String, startIndex: Int): AnnotationMatch? {
        var cursor = startIndex + 1
        if (cursor >= content.length || !isAnnotationNameChar(content[cursor])) return null

        while (cursor < content.length && isAnnotationNameChar(content[cursor])) {
            cursor++
        }

        val annotationName = content.substring(startIndex + 1, cursor)
        val simpleName = annotationName.substringAfterLast(':').substringAfterLast('.')

        cursor = skipWhitespace(content, cursor)
        val arguments = if (cursor < content.length && content[cursor] == '(') {
            val endIndex = findClosingParenthesis(content, cursor)
            if (endIndex == -1) return null
            val raw = content.substring(cursor, endIndex + 1)
            cursor = endIndex + 1
            raw
        } else {
            null
        }

        return AnnotationMatch(
            simpleName = simpleName,
            arguments = arguments,
            startIndex = startIndex,
            endIndex = cursor,
            declarationIndex = findDeclarationAnchor(content, cursor),
        )
    }

    private fun findDeclarationAnchor(content: String, startIndex: Int): Int {
        var index = startIndex

        while (index < content.length) {
            index = skipWhitespace(content, index)
            if (index >= content.length) return -1

            if (content[index] == '@' && isAnnotationStart(content, index)) {
                index = skipAnnotation(content, index)
                continue
            }

            return index
        }

        return -1
    }

    private fun skipAnnotation(content: String, startIndex: Int): Int {
        var cursor = startIndex + 1
        while (cursor < content.length && isAnnotationNameChar(content[cursor])) {
            cursor++
        }

        cursor = skipWhitespace(content, cursor)
        if (cursor < content.length && content[cursor] == '(') {
            val endIndex = findClosingParenthesis(content, cursor)
            if (endIndex != -1) {
                return endIndex + 1
            }
        }

        return cursor
    }

    private fun parseArguments(content: String?): ParsedArguments {
        if (content.isNullOrBlank()) return ParsedArguments()

        val body = content
            .trim()
            .removeSurrounding("(", ")")
            .trim()
        if (body.isBlank()) return ParsedArguments()

        val named = linkedMapOf<String, String>()
        val positional = mutableListOf<String>()

        splitTopLevel(body).forEach { segment ->
            val separatorIndex = findTopLevelEquals(segment)
            if (separatorIndex == -1) {
                positional += segment.trim()
            } else {
                val key = segment.substring(0, separatorIndex).trim()
                val value = segment.substring(separatorIndex + 1).trim()
                if (key.isEmpty()) {
                    positional += segment.trim()
                } else {
                    named[key] = value
                }
            }
        }

        return ParsedArguments(named, positional)
    }

    private fun splitTopLevel(content: String): List<String> {
        if (content.isBlank()) return emptyList()

        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var parentheses = 0
        var braces = 0
        var brackets = 0
        var angleBrackets = 0
        var index = 0

        while (index < content.length) {
            when {
                content.startsWith(KOTLIN_RAW_STRING, index) -> {
                    val endIndex = skipRawString(content, index)
                    current.append(content, index, endIndex)
                    index = endIndex
                    continue
                }

                content[index] == '"' -> {
                    val endIndex = skipQuoted(content, index, '"')
                    current.append(content, index, endIndex)
                    index = endIndex
                    continue
                }

                content[index] == '\'' -> {
                    val endIndex = skipQuoted(content, index, '\'')
                    current.append(content, index, endIndex)
                    index = endIndex
                    continue
                }
            }

            when (content[index]) {
                '(' -> parentheses++
                ')' -> parentheses--
                '{' -> braces++
                '}' -> braces--
                '[' -> brackets++
                ']' -> brackets--
                '<' -> angleBrackets++
                '>' -> angleBrackets = (angleBrackets - 1).coerceAtLeast(0)
                ',' -> {
                    if (parentheses == 0 && braces == 0 && brackets == 0 && angleBrackets == 0) {
                        val part = current.toString().trim()
                        if (part.isNotEmpty()) parts += part
                        current.clear()
                        index++
                        continue
                    }
                }
            }

            current.append(content[index])
            index++
        }

        val tail = current.toString().trim()
        if (tail.isNotEmpty()) {
            parts += tail
        }

        return parts
    }

    private fun findTopLevelEquals(content: String): Int {
        return findTopLevelSeparator(content, '=')
    }

    private fun findTopLevelSeparator(content: String, separator: Char): Int {
        var parentheses = 0
        var braces = 0
        var brackets = 0
        var angleBrackets = 0
        var index = 0

        while (index < content.length) {
            when {
                content.startsWith(KOTLIN_RAW_STRING, index) -> {
                    index = skipRawString(content, index)
                    continue
                }

                content[index] == '"' -> {
                    index = skipQuoted(content, index, '"')
                    continue
                }

                content[index] == '\'' -> {
                    index = skipQuoted(content, index, '\'')
                    continue
                }
            }

            when (content[index]) {
                '(' -> parentheses++
                ')' -> parentheses--
                '{' -> braces++
                '}' -> braces--
                '[' -> brackets++
                ']' -> brackets--
                '<' -> angleBrackets++
                '>' -> angleBrackets = (angleBrackets - 1).coerceAtLeast(0)
                separator -> {
                    if (parentheses == 0 && braces == 0 && brackets == 0 && angleBrackets == 0) {
                        return index
                    }
                }
            }

            index++
        }

        return -1
    }

    private fun parseInt(value: String?, default: Int): Int {
        if (value == null) return default
        return value
            .trim()
            .replace("_", "")
            .toIntOrNull()
            ?: default
    }

    private fun parseString(value: String?, default: String): String {
        return parseStringOrNull(value) ?: default
    }

    private fun parseStringOrNull(value: String?): String? {
        if (value == null) return null

        val trimmed = value.trim()
        return when {
            trimmed.startsWith(KOTLIN_RAW_STRING) && trimmed.endsWith(KOTLIN_RAW_STRING) && trimmed.length >= 6 -> {
                trimmed.substring(3, trimmed.length - 3)
            }

            trimmed.startsWith('"') && trimmed.endsWith('"') && trimmed.length >= 2 -> {
                unescapeString(trimmed.substring(1, trimmed.length - 1))
            }

            else -> null
        }
    }

    private fun parseArray(value: String?, default: List<String>): List<String> {
        if (value == null) return default

        val trimmed = value.trim()
        val body = unwrapArray(trimmed) ?: trimmed
        val items = splitTopLevel(body)
            .mapNotNull(::parseStringOrNull)

        return items.ifEmpty { default }
    }

    private fun unwrapArray(value: String): String? {
        return when {
            value.startsWith('{') && value.endsWith('}') -> value.substring(1, value.length - 1)
            value.startsWith('[') && value.endsWith(']') -> value.substring(1, value.length - 1)
            ARRAY_CALL_REGEX.matches(value) -> ARRAY_CALL_REGEX.matchEntire(value)?.groupValues?.get(1)
            else -> null
        }
    }

    private fun unescapeString(value: String): String {
        val result = StringBuilder(value.length)
        var index = 0

        while (index < value.length) {
            val char = value[index]
            if (char != '\\' || index == value.lastIndex) {
                result.append(char)
                index++
                continue
            }

            val escaped = value[index + 1]
            when (escaped) {
                '\\' -> result.append('\\')
                '"' -> result.append('"')
                '\'' -> result.append('\'')
                'n' -> result.append('\n')
                'r' -> result.append('\r')
                't' -> result.append('\t')
                'b' -> result.append('\b')
                '$' -> result.append('$')
                'u' -> {
                    val unicode = value.substring(index + 2, (index + 6).coerceAtMost(value.length))
                    val decoded = unicode.takeIf { it.length == 4 }?.toIntOrNull(16)
                    if (decoded != null) {
                        result.append(decoded.toChar())
                        index += 6
                        continue
                    } else {
                        result.append('u')
                    }
                }

                else -> result.append(escaped)
            }

            index += 2
        }

        return result.toString()
    }

    private fun stripComments(content: String): String {
        val result = StringBuilder(content.length)
        var index = 0

        while (index < content.length) {
            when {
                content.startsWith(KOTLIN_RAW_STRING, index) -> {
                    val endIndex = skipRawString(content, index)
                    result.append(content, index, endIndex)
                    index = endIndex
                }

                content[index] == '"' -> {
                    val endIndex = skipQuoted(content, index, '"')
                    result.append(content, index, endIndex)
                    index = endIndex
                }

                content[index] == '\'' -> {
                    val endIndex = skipQuoted(content, index, '\'')
                    result.append(content, index, endIndex)
                    index = endIndex
                }

                content[index] == '/' && index + 1 < content.length && content[index + 1] == '/' -> {
                    result.append("  ")
                    index += 2
                    while (index < content.length && content[index] != '\n') {
                        result.append(' ')
                        index++
                    }
                }

                content[index] == '/' && index + 1 < content.length && content[index + 1] == '*' -> {
                    result.append("  ")
                    index += 2
                    while (index < content.length) {
                        if (content[index] == '\n' || content[index] == '\r') {
                            result.append(content[index])
                            index++
                            continue
                        }

                        if (content[index] == '*' && index + 1 < content.length && content[index + 1] == '/') {
                            result.append("  ")
                            index += 2
                            break
                        }

                        result.append(' ')
                        index++
                    }
                }

                else -> {
                    result.append(content[index])
                    index++
                }
            }
        }

        return result.toString()
    }

    private fun findClosingParenthesis(content: String, openIndex: Int): Int {
        var depth = 0
        var index = openIndex

        while (index < content.length) {
            when {
                content.startsWith(KOTLIN_RAW_STRING, index) -> {
                    index = skipRawString(content, index)
                    continue
                }

                content[index] == '"' -> {
                    index = skipQuoted(content, index, '"')
                    continue
                }

                content[index] == '\'' -> {
                    index = skipQuoted(content, index, '\'')
                    continue
                }
            }

            when (content[index]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) {
                        return index
                    }
                }
            }

            index++
        }

        return -1
    }

    private fun skipWhitespace(content: String, startIndex: Int): Int {
        var index = startIndex
        while (index < content.length && content[index].isWhitespace()) {
            index++
        }
        return index
    }

    private fun isTopLevelDeclaration(content: String, declarationIndex: Int): Boolean {
        var braceDepth = 0
        var index = 0

        while (index < declarationIndex) {
            when {
                content.startsWith(KOTLIN_RAW_STRING, index) -> {
                    index = skipRawString(content, index)
                    continue
                }

                content[index] == '"' -> {
                    index = skipQuoted(content, index, '"')
                    continue
                }

                content[index] == '\'' -> {
                    index = skipQuoted(content, index, '\'')
                    continue
                }
            }

            when (content[index]) {
                '{' -> braceDepth++
                '}' -> braceDepth--
            }
            index++
        }

        return braceDepth == 0
    }

    private fun skipRawString(content: String, startIndex: Int): Int {
        val endIndex = content.indexOf(KOTLIN_RAW_STRING, startIndex + KOTLIN_RAW_STRING.length)
        return if (endIndex == -1) {
            content.length
        } else {
            endIndex + KOTLIN_RAW_STRING.length
        }
    }

    private fun skipQuoted(content: String, startIndex: Int, quote: Char): Int {
        var index = startIndex + 1
        while (index < content.length) {
            if (content[index] == '\\') {
                index += 2
                continue
            }

            if (content[index] == quote) {
                return index + 1
            }

            index++
        }

        return content.length
    }

    private fun isAnnotationStart(content: String, index: Int): Boolean {
        if (index <= 0) return true
        return !isAnnotationNameChar(content[index - 1])
    }

    private fun isAnnotationNameChar(char: Char): Boolean {
        return char.isLetterOrDigit() || char == '_' || char == '.' || char == ':'
    }

    private fun isIdentifierStart(char: Char): Boolean {
        return char.isLetter() || char == '_'
    }

    private fun isSimpleIdentifier(content: String): Boolean {
        return content.isNotEmpty() &&
            isIdentifierStart(content.first()) &&
            content.drop(1).all { it.isLetterOrDigit() || it == '_' }
    }

    private fun skipIdentifier(content: String, startIndex: Int): Int {
        var index = startIndex
        while (index < content.length && (content[index].isLetterOrDigit() || content[index] == '_')) {
            index++
        }
        return index
    }

    private fun isFunctionKeyword(content: String, index: Int): Boolean {
        return content.startsWith(FUNCTION_KEYWORD, index) &&
            (index + FUNCTION_KEYWORD.length >= content.length || !content[index + FUNCTION_KEYWORD.length].isLetterOrDigit() && content[index + FUNCTION_KEYWORD.length] != '_')
    }

    private fun invalidEntryFunction(file: File, message: String): IllegalArgumentException {
        return IllegalArgumentException(
            "Invalid @XplerHint entry in `${file.path}`: $message Use `fun init(module: XplerModuleInterface)`."
        )
    }

    private data class AnnotationMatch(
        val simpleName: String,
        val arguments: String?,
        val startIndex: Int,
        val endIndex: Int,
        val declarationIndex: Int,
    )

    private data class ParsedArguments(
        val named: Map<String, String> = emptyMap(),
        val positional: List<String> = emptyList(),
    ) {
        fun value(name: String, position: Int): String? {
            return named[name] ?: positional.getOrNull(position)
        }
    }

    private class MutableXplerEntryBean(
        var xplerHintBean: XplerHintBean? = null,
        var xposedHintBean: XposedHintBean? = null,
    )
}
