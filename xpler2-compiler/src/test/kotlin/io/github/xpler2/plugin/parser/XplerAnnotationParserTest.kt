package io.github.xpler2.plugin.parser

import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class XplerAnnotationParserTest {
    @Test
    fun `parses a valid annotated top level entry`() {
        val source = sourceFile(
            """
            package example.module

            @XposedHint(version = 90)
            @XplerHint(
                name = "example.generated.Entry",
                description = "Example module",
                scope = ["example.one", "example.two"],
            )
            fun init(module: XplerModuleInterface) = Unit
            """.trimIndent()
        )

        val entry = XplerAnnotationParser.parser(source.toFile(), "FallbackEntry").single()
        assertEquals("example.module", entry.function.packageName)
        assertEquals("init", entry.function.functionName)
        assertEquals("example.generated.Entry", entry.xplerHint.resolvedName)
        assertEquals(listOf("example.one", "example.two"), entry.xplerHint.scope)
        assertEquals(90, entry.xposedHint?.version)
    }

    @Test
    fun `ignores annotations inside comments and strings`() {
        val source = sourceFile(
            """
            package example.module

            // @XplerHint(description = "Ignored", scope = ["ignored"])
            val sample = "@XplerHint(description = \"Ignored\", scope = [\"ignored\"])"

            @XplerHint(description = "Actual", scope = ["example.target"])
            fun init(module: XplerModuleInterface) = Unit
            """.trimIndent()
        )

        val entries = XplerAnnotationParser.parser(source.toFile(), "GeneratedEntry")
        assertEquals(1, entries.size)
        assertEquals("Actual", entries.single().xplerHint.description)
    }

    @Test
    fun `rejects entry functions with multiple parameters`() {
        val source = sourceFile(
            """
            package example.module

            @XplerHint(description = "Invalid", scope = ["example.target"])
            fun init(module: XplerModuleInterface, extra: String) = Unit
            """.trimIndent()
        )

        val error = assertFailsWith<IllegalArgumentException> {
            XplerAnnotationParser.parser(source.toFile(), "GeneratedEntry")
        }
        assertTrue(error.message.orEmpty().contains("exactly one parameter"))
    }

    private fun sourceFile(content: String): Path {
        return createTempFile("xpler2-parser-test", ".kt").also { it.writeText(content) }
    }
}
