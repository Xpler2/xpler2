package io.github.xpler2.plugin.asm

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import io.github.xpler2.plugin.asm.cls.LsposedClassVisitor
import io.github.xpler2.plugin.asm.cls.XplerBaseModuleClassVisitor
import io.github.xpler2.plugin.asm.cls.XplerHookerEntitiesClassVisitor
import io.github.xpler2.plugin.asm.cls.XplerInitClassVisitor
import io.github.xpler2.plugin.asm.cls.XplerModuleStatusClassVisitor
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File

abstract class ASMVisitorFactory : AsmClassVisitorFactory<ASMVisitorFactory.Params> {

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

        val cls = classContext.currentClassData.className
        return when {
            cls.startsWith("io.github.libxposed.") -> LsposedClassVisitor(
                api = Opcodes.ASM9,
                classVisitor = nextClassVisitor,
                initializeCache = initializeCache,
                applicationId = params.applicationId,
                variant = params.variant,
            )

            cls == "io.github.xpler2.hooker.HookerEntities" -> XplerHookerEntitiesClassVisitor(
                api = Opcodes.ASM9,
                classVisitor = nextClassVisitor,
                initializeCache = initializeCache,
                applicationId = params.applicationId,
                variant = params.variant,
            )

            cls == "io.github.xpler2.base.BaseModule" -> XplerBaseModuleClassVisitor(
                api = Opcodes.ASM9,
                classVisitor = nextClassVisitor,
                initializeCache = initializeCache,
                applicationId = params.applicationId,
                variant = params.variant,
            )

            cls == $$"io.github.xpler2.XplerModuleStatus$Companion" -> XplerModuleStatusClassVisitor(
                api = Opcodes.ASM9,
                classVisitor = nextClassVisitor,
                initializeCache = initializeCache,
                applicationId = params.applicationId,
                variant = params.variant,
            )

            else -> XplerInitClassVisitor(
                api = Opcodes.ASM9,
                classVisitor = nextClassVisitor,
                initializeCache = initializeCache,
                applicationId = params.applicationId,
                variant = params.variant,
            )
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val params = parameters.get()
        val initializeCache = XplerInitializeCache.cache(params.cacheDirectory)
            ?: throw NullPointerException("Xpler2 compiler cache is not found. ")
        val sourceName = initializeCache.sourcePath
            .replace(".java", "")
            .replace(".kt", "Kt")
            .replace(File.separator, ".")

        if (classData.className.startsWith("io.github.libxposed.")) {
            return true
        }

        if (classData.className.startsWith("io.github.xpler2.")) {
            return true
        }

        if (classData.interfaces.any { it.startsWith("io.github.xpler2.") }) {
            return true
        }

        if (classData.classAnnotations.any { it.startsWith("io.github.xpler2.") }) {
            return true
        }

        if (sourceName.indexOf(classData.className) != -1) {
            return true
        }

        return false
    }
}