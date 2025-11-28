package io.github.xpler2.plugin.asm.generate

import io.github.xpler2.plugin.asm.base.BaseGenerate
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File

object XposedInitGenerate : BaseGenerate() {
    private const val BASE_XPOSED = "io/github/xpler2/base/XposedEntrance"

    var isEnabled: Boolean = false
    var initClassName: String? = null
    var domainClassName: String? = null
    var domainMethodName: String? = null

    override fun reset() {
        isEnabled = false
        initClassName = null
        domainClassName = null
        domainMethodName = null
    }

    override fun finish(path: File): String? {
        if (!isEnabled) return null
        if (initClassName == null) throw NullPointerException("XposedInitGenerate.xposedInit is null")
        if (domainClassName == null) throw NullPointerException("XposedInitGenerate.domainClassName is null")
        if (domainMethodName == null) throw NullPointerException("XposedInitGenerate.domainMethodName is null")

        return path.resolve("$initClassName.class").also {
            it.delete()
            it.parentFile.mkdirs()
            println("[XposedInit]: ${it.absolutePath}")
            it.writeBytes(generateByteCode())
        }.absolutePath
    }

    override fun generateByteCode(): ByteArray {
        return ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).apply {
            visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL or Opcodes.ACC_SUPER,
                initClassName,
                null,
                BASE_XPOSED,
                null,
            )

            // constructor
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
            ).apply {
                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    BASE_XPOSED,
                    "<init>",
                    "()V",
                    false
                )
                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            // handleLoadPackage
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "handleLoadPackage",
                $$"(Lde/robv/android/xposed/callbacks/XC_LoadPackage$LoadPackageParam;)V",
                null,
                null
            ).apply {
                visitAnnotation("Ljava/lang/Override;", true)
                    .apply {
                        visitEnd()
                    }

                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    BASE_XPOSED,
                    "handleLoadPackage",
                    $$"(Lde/robv/android/xposed/callbacks/XC_LoadPackage$LoadPackageParam;)V",
                    false
                )

                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    initClassName,
                    "getModule",
                    "()Lio/github/xpler2/XplerModuleInterface;",
                    false
                )

                visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    domainClassName,
                    domainMethodName,
                    "(Lio/github/xpler2/XplerModuleInterface;)V",
                    false,
                )

                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            visitEnd()
        }.toByteArray()
    }
}