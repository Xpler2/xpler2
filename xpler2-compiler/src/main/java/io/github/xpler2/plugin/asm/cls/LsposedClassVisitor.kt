package io.github.xpler2.plugin.asm.cls

import io.github.xpler2.plugin.asm.base.BaseClassVisitor
import io.github.xpler2.plugin.asm.method.LsposedMethodVisitor
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class LsposedClassVisitor(
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
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        // if the annotation is not compatible with the old API, the `XposedHooker` annotation will be removed.
        val annotation = Type.getType(descriptor)
        if (!initial.lsposedCompatAnnotation
            && annotation.className == "io.github.libxposed.api.annotations.XposedHooker"
        ) {
            return null
        }

        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?,
    ): MethodVisitor? {
        // if only one parameter, check if it is a XposedInterface.BeforeHookCallback or XposedInterface.AfterHookCallback
        val isPublicStatic = Opcodes.ACC_PUBLIC and access != 0 && Opcodes.ACC_STATIC and access != 0
        val argumentTypes = Type.getArgumentTypes(descriptor)
        val singleParam = argumentTypes.singleOrNull()
        if (isPublicStatic
            && (singleParam?.className == $$"io.github.libxposed.api.XposedInterface$BeforeHookCallback"
                    || singleParam?.className == $$"io.github.libxposed.api.XposedInterface$AfterHookCallback")
        ) {
            return LsposedMethodVisitor(
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