package io.github.xpler2.plugin.asm.cls

import io.github.xpler2.plugin.asm.base.BaseClassVisitor
import io.github.xpler2.plugin.asm.method.XplerInitializeMethodVisitor
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class XplerInitializeClassVisitor(
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
        // if only one parameter, check if it is a XplerModuleInterface
        val isPublicStatic = Opcodes.ACC_PUBLIC and access != 0 && Opcodes.ACC_STATIC and access != 0
        val argumentTypes = Type.getArgumentTypes(descriptor)
        val singleParam = argumentTypes.singleOrNull()
        if (isPublicStatic && singleParam?.className == "io.github.xpler2.XplerModuleInterface") {
            return XplerInitializeMethodVisitor(
                api = api,
                methodVisitor = super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions
                ),
                ownerName = ownerName,
                methodMame = name,
                descriptor = descriptor,
                initializeCache = initializeCache,
                applicationId = applicationId,
                variant = variant,
            )
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}