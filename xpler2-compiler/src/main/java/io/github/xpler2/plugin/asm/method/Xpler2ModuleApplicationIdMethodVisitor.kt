package io.github.xpler2.plugin.asm.method

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.bean.XplerInitializeCache
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

// Inject the module applicationId
class Xpler2ModuleApplicationIdMethodVisitor(
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
            mv.visitLdcInsn(applicationId)
        }
        super.visitInsn(opcode)
    }
}