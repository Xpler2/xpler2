package io.github.xpler2.plugin.asm.method

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class LsposedMethodVisitor(
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

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        val annotation = Type.getType(descriptor)

        // If the annotation is not compatible with the old API, the `BeforeInvocation` and `AfterInvocation` annotations will be removed
        if (!initial.lsposedCompatAnnotation
            && (annotation.className == "io.github.libxposed.api.annotations.BeforeInvocation"
                    || annotation.className == "io.github.libxposed.api.annotations.AfterInvocation")
        ) {
            return null
        }

        return super.visitAnnotation(descriptor, visible)
    }
}