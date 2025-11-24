package io.github.xpler2.plugin.asm.cls

import io.github.xpler2.plugin.asm.base.BaseClassVisitor
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class XplerHookerEntitiesClassVisitor(
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
        if (name == "collect") {
            return null // return null Achieve the goal of refactoring
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    override fun visitEnd() {
        cv.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
            "collect",
            "()V",
            null,
            null
        ).apply {
            visitCode()

            // call: this.registerHooker(io.github.xpler2.hooker.HookerEntitiesStore.getEntities()) // getEntities() return Set<io.github.xpler2.hookerHookerEntity>
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "io/github/xpler2/hooker/HookerEntitiesStore",
                "getEntities",
                "()Ljava/util/Set;",
                false
            )
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                ownerName,
                "registerHooker",
                "(Ljava/util/Set;)V",
                false
            )

            visitInsn(Opcodes.RETURN)
            visitMaxs(2, 1)
            visitEnd()
        }

        super.visitEnd()
    }
}