package io.github.xpler2.plugin.asm.base

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.cache.XplerInitializeCache
import org.objectweb.asm.ClassVisitor

open class BaseClassVisitor(
    api: Int,
    classVisitor: ClassVisitor,
    val initializeCache: XplerInitializeCache,
    val applicationId: String?,
    val variant: String,
) : ClassVisitor(api, classVisitor) {
    private lateinit var mOwnerName: String
    private var mSuperName: String? = null

    val ownerName: String
        get() = mOwnerName

    val superName: String?
        get() = mSuperName

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
        mOwnerName = name
        mSuperName = superName
        super.visit(version, access, name, signature, superName, interfaces)
    }
}