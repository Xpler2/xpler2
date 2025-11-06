package io.github.xpler2.plugin.asm

import io.github.xpler2.plugin.asm.method.Xpler2ModuleApplicationIdMethodVisitor
import io.github.xpler2.plugin.asm.method.Xpler2ModuleInitMethodVisitor
import io.github.xpler2.plugin.asm.method.Xpler2ModuleStatusMethodVisitor
import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.bean.XplerInitializeCache
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class Xpler2ClassVisitor(
    api: Int,
    classVisitor: ClassVisitor,
    private val initializeCache: XplerInitializeCache,
    private val applicationId: String?,
    private val variant: String,
) : ClassVisitor(api, classVisitor) {
    private lateinit var mOwnerName: String
    private var mSuperName: String? = null

    val initial: XplerInitializeBean
        get() = initializeCache.initializeBean

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?,
    ) {
        mOwnerName = name.replace('/', '.')
        mSuperName = superName?.replace('/', '.')
        super.visit(version, access, name, signature, superName, interfaces)
    }

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
        // if only one parameter, check if it is a XplerModuleInterface or XposedInterface.BeforeHookCallback or XposedInterface.AfterHookCallback
        val isPublicStatic = Opcodes.ACC_PUBLIC and access != 0 && Opcodes.ACC_STATIC and access != 0
        val argumentTypes = Type.getArgumentTypes(descriptor)
        val singleParam = argumentTypes.singleOrNull()
        if (isPublicStatic
            && (singleParam?.className == "io.github.xpler2.XplerModuleInterface"
                    || singleParam?.className == "io.github.libxposed.api.XposedInterface\$BeforeHookCallback"
                    || singleParam?.className == "io.github.libxposed.api.XposedInterface\$AfterHookCallback")
        ) {
            return Xpler2ModuleInitMethodVisitor(
                api = api,
                methodVisitor = super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions
                ),
                ownerName = mOwnerName,
                methodMame = name,
                descriptor = descriptor,
                initializeCache = initializeCache,
                applicationId = applicationId,
                variant = variant,
            )
        }

        // if the method is `getModuleApplicationId` and the owner is `BaseModule`, handle it.
        if (name == "getModuleApplicationId" && mOwnerName == "io.github.xpler2.base.BaseModule") {
            return Xpler2ModuleApplicationIdMethodVisitor(
                api = api,
                methodVisitor = super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions
                ),
                ownerName = mOwnerName,
                methodMame = name,
                descriptor = descriptor,
                initializeCache = initializeCache,
                applicationId = applicationId,
                variant = variant,
            )
        }

        // if the return type is `XplerModuleStatus`, check if the method is `getInstance`.
        val returnType = Type.getReturnType(descriptor)
        if (name == "getInstance"
            && mOwnerName == "io.github.xpler2.XplerModuleStatus\$Companion"
            && returnType.className == "io.github.xpler2.XplerModuleStatus"
        ) {
            return Xpler2ModuleStatusMethodVisitor(
                api = api,
                methodVisitor = super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions
                ),
                ownerName = mOwnerName,
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