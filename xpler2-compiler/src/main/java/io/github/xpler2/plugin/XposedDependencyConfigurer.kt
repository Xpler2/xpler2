package io.github.xpler2.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

internal object XposedDependencyConfigurer {
    val bundledCompileOnlyResourcePaths = listOf("xposed/api-82.jar")

    fun isRuntimeAvailable(project: Project): Boolean {
        return project.configurations.any { configuration ->
            configuration.dependencies.any(::isRuntimeDependency)
        }
    }

    internal fun isRuntimeDependency(dependency: Dependency): Boolean {
        return dependency.name == XPLER2_XPOSED_ARTIFACT_NAME
    }

    private const val XPLER2_XPOSED_ARTIFACT_NAME = "xpler2-xposed"
}
