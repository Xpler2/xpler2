package io.github.xpler2.plugin.asm.method

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.bean.XplerInitializeCache
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

// Inject module status
class Xpler2ModuleStatusMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    private val ownerName: String,
    private val methodMame: String,
    private val descriptor: String,
    private val initializeCache: XplerInitializeCache,
    private val applicationId: String?,
    private val variant: String,
) : MethodVisitor(api, methodVisitor) {

    val initial: XplerInitializeBean
        get() = initializeCache.initializeBean

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.ARETURN) { // If the opcode is `ARETURN`, it means we are returning an object
            mv.visitInsn(Opcodes.POP) // Remove the return value, as we are replacing the method

            if (initial.lsposed) {
                // If the method is `getInstance` of `XplerModuleStatus`, replace it with `LsposedStatus.INSTANCE`
                mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "io/github/xpler2/base/LsposedStatus",
                    "INSTANCE",
                    "Lio/github/xpler2/base/LsposedStatus;"
                )
            } else if (initial.xposed) {
                // If the method is `getInstance` of `XplerModuleStatus`, replace it with `XposedStatus.INSTANCE`
                mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "io/github/xpler2/base/XposedStatus",
                    "INSTANCE",
                    "Lio/github/xpler2/base/XposedStatus;"
                )
            } else {
                // todo
            }
        }

        super.visitInsn(opcode) // Call the superclass method to continue the visit
    }
}