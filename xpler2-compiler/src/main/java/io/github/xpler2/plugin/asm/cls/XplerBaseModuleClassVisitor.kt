package io.github.xpler2.plugin.asm.cls

import io.github.xpler2.plugin.asm.base.BaseClassVisitor
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class XplerBaseModuleClassVisitor(
    api: Int,
    classVisitor: ClassVisitor,
    initializeCache: XplerInitializeCache,
    applicationId: String?,
    variant: String,
) : BaseClassVisitor(
    api,
    classVisitor,
    initializeCache,
    applicationId,
    variant
) {
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?,
    ): MethodVisitor? {
        // if the method is `getModuleApplicationId`.
        if (name == "getModuleApplicationId") {
            return null // return null Achieve the goal of refactoring
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    override fun visitEnd() {
        cv.visitMethod(
            Opcodes.ACC_PUBLIC,
            "getModuleApplicationId",
            "()Ljava/lang/String;",
            null,
            null,
        ).apply {
            visitCode()
            visitLdcInsn(applicationId)
            visitInsn(Opcodes.ARETURN)
            visitMaxs(1, 1)
            visitEnd()
        }

        super.visitEnd()
    }
}