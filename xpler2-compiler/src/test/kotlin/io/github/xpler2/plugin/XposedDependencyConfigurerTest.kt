package io.github.xpler2.plugin

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class XposedDependencyConfigurerTest {
    @Test
    fun `does not require xposed support for a new project`() {
        val project = ProjectBuilder.builder().build()
        project.configurations.create("implementation")

        assertFalse(XposedDependencyConfigurer.isRuntimeAvailable(project))
    }

    @Test
    fun `detects the published xposed runtime dependency`() {
        val project = ProjectBuilder.builder().build()
        project.configurations.create("implementation")
        project.dependencies.add(
            "implementation",
            "io.github.xpler2:xpler2-xposed:0.0.20",
        )

        assertTrue(XposedDependencyConfigurer.isRuntimeAvailable(project))
    }
}
