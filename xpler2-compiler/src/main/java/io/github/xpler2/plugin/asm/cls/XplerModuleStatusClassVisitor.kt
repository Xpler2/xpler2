package io.github.xpler2.plugin.asm.cls

import io.github.xpler2.plugin.asm.base.BaseClassVisitor
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class XplerModuleStatusClassVisitor(
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
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String?>?,
    ): MethodVisitor? {
        if (name == "getInstance") {
            return null // return null Achieve the goal of refactoring
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    override fun visitEnd() {
        cv.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
            "getInstance",
            "()Lio/github/xpler2/XplerModuleStatus;",
            null,
            null,
        ).apply {
            visitCode()

            if (initial.lsposed) { // `io/github/xpler2/base/LsposedStatus.INSTANCE`
                visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "io/github/xpler2/base/LsposedStatus",
                    "INSTANCE",
                    "Lio/github/xpler2/base/LsposedStatus;"
                )
            } else if (initial.xposed) { // `io/github/xpler2/base/XposedStatus.INSTANCE`
                visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "io/github/xpler2/base/XposedStatus",
                    "INSTANCE",
                    "Lio/github/xpler2/base/XposedStatus;"
                )
            } else { // null
                visitInsn(Opcodes.ACONST_NULL)
            }

            visitInsn(Opcodes.ARETURN)
            visitMaxs(1, 1)
            visitEnd()
        }

        super.visitEnd()
    }
}