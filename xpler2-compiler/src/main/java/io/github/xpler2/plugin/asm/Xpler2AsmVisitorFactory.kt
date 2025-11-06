package io.github.xpler2.plugin.asm

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import io.github.xpler2.plugin.compiler.bean.XplerInitializeCache
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File


abstract class Xpler2AsmVisitorFactory : AsmClassVisitorFactory<Xpler2AsmVisitorFactory.Params> {

    interface Params : InstrumentationParameters {
        @get:InputDirectory
        @get:PathSensitive(PathSensitivity.RELATIVE)
        var cacheDirectory: Directory

        @get:Input
        var applicationId: String?

        @get:Input
        var variant: String
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor,
    ): ClassVisitor {
        val params = parameters.get()
        val initializeCache = XplerInitializeCache.cache(params.cacheDirectory)
            ?: throw NullPointerException("Xpler2 compiler cache is not found. ")
        return Xpler2ClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = nextClassVisitor,
            initializeCache = initializeCache,
            applicationId = params.applicationId,
            variant = params.variant,
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val params = parameters.get()
        val initializeCache = XplerInitializeCache.cache(params.cacheDirectory)
            ?: throw NullPointerException("Xpler2 compiler cache is not found. ")
        val sourceName = initializeCache.sourcePath
            .replace(".java", "")
            .replace(".kt", "Kt")
            .replace(File.separator, ".")

        return (classData.className.indexOf("io.github.libxposed.") != -1
                || classData.className.indexOf("io.github.xpler2.") != -1
                || classData.interfaces.indexOfFirst { it.startsWith("io.github.xpler2.") } != -1
                || sourceName.indexOf(classData.className) != -1)
    }
}