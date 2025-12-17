package io.github.xpler2.plugin.asm.generate

import io.github.xpler2.plugin.asm.base.BaseGenerate
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File

object LsposedInitGenerate : BaseGenerate() {
    private const val BASE_LSPOSED = "io/github/xpler2/base/LsposedEntrance"

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

    override fun finish(path: File): Set<String> {
        if (!isEnabled) return emptySet()
        requireNotNull(initClassName) { "LsposedInitGenerate.lsposedInit is null" }
        requireNotNull(domainClassName) { "LsposedInitGenerate.domainClassName is null" }
        requireNotNull(domainMethodName) { "LsposedInitGenerate.domainMethodName is null" }

        return setOf(
            path.resolve("$initClassName.class").also {
                it.delete()
                it.parentFile.mkdirs()
                println("[LsposedInit]: ${it.absolutePath}")
                it.writeBytes(generateByteCode())
            }.absolutePath
        )
    }

    override fun generateByteCode(): ByteArray {
        return ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).apply {
            visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL or Opcodes.ACC_SUPER,
                initClassName,
                null,
                BASE_LSPOSED,
                null,
            )

            // constructor
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                $$"(Lio/github/libxposed/api/XposedInterface;Lio/github/libxposed/api/XposedModuleInterface$ModuleLoadedParam;)V",
                null,
                null
            ).apply {
                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitVarInsn(Opcodes.ALOAD, 2)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    BASE_LSPOSED,
                    "<init>",
                    $$"(Lio/github/libxposed/api/XposedInterface;Lio/github/libxposed/api/XposedModuleInterface$ModuleLoadedParam;)V",
                    false
                )
                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            // onPackageLoaded
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "onPackageLoaded",
                $$"(Lio/github/libxposed/api/XposedModuleInterface$PackageLoadedParam;)V",
                null,
                null
            ).apply {
                visitAnnotation("Ljava/lang/Override;", true).apply {
                    visitEnd()
                }

                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    BASE_LSPOSED,
                    "onPackageLoaded",
                    $$"(Lio/github/libxposed/api/XposedModuleInterface$PackageLoadedParam;)V",
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