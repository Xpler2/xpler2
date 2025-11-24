package io.github.xpler2.plugin.asm.method

import io.github.xpler2.plugin.asm.generate.LsposedInitGenerate
import io.github.xpler2.plugin.asm.generate.XposedInitGenerate
import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

// Inject module initialization
class Xpler2InitMethodVisitor(
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

        // process the `XplerInitialize` annotation
        if (annotation.className == "io.github.xpler2.XplerInitialize") {
            setGenerateXposedInitClassConfig()
            setGenerateLsposedInitClassConfig()
            return null // remove the annotation (no bytecode reserved)
        }

        return super.visitAnnotation(descriptor, visible)
    }

    // generate `XposedInit` class config
    private fun setGenerateXposedInitClassConfig() {
        XposedInitGenerate.isEnabled = initial.xposed
        XposedInitGenerate.initClassName = initial.xposedInit.replace(".", "/")
        XposedInitGenerate.domainClassName = ownerName
        XposedInitGenerate.domainMethodName = methodMame
    }

    // generate `LsposedInit` class config
    private fun setGenerateLsposedInitClassConfig() {
        if (!initial.lsposed) return
        LsposedInitGenerate.isEnabled = initial.lsposed
        LsposedInitGenerate.initClassName = initial.lsposedInit.replace(".", "/")
        LsposedInitGenerate.domainClassName = ownerName
        LsposedInitGenerate.domainMethodName = methodMame
    }
}