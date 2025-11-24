package io.github.xpler2.plugin.asm.generate

import io.github.xpler2.plugin.asm.base.BaseGenerate
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

object HookerEntitiesGenerate : BaseGenerate() {
    private const val CLASS_NAME = "io/github/xpler2/hooker/HookerEntitiesStore"
    private const val ENTITIES_NAME = "getEntities"
    private val collectedEntities = ConcurrentLinkedQueue<String>()

    fun addEntity(entityName: String) {
        collectedEntities.add(entityName)
    }

    override fun reset() {
        collectedEntities.clear()
    }

    override fun finish(path: File): String? {
        return path.resolve("$CLASS_NAME.class")
            .also {
                it.delete()
                it.parentFile.mkdirs()
                println("HookerEntitiesStore class: ${it.absolutePath}")
                it.writeBytes(generateByteCode())
            }.absolutePath
    }

    override fun generateByteCode(): ByteArray {
        return ClassWriter(
            ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS,
        ).apply {
            visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL,
                CLASS_NAME,
                null,
                "java/lang/Object",
                null
            )

            // Generate getEntities()
            visitMethod(
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
                ENTITIES_NAME,
                "()Ljava/util/Set;",
                null,
                null
            ).apply {
                visitCode()

                // new ArrayList()
                visitTypeInsn(Opcodes.NEW, "java/util/HashSet")
                visitInsn(Opcodes.DUP)
                visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false)

                // iterate through the collected classes and generate add code
                collectedEntities.forEach { entity ->
                    visitInsn(Opcodes.DUP)
                    visitTypeInsn(Opcodes.NEW, entity)
                    visitInsn(Opcodes.DUP)
                    visitMethodInsn(Opcodes.INVOKESPECIAL, entity, "<init>", "()V", false)
                    visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashSet", "add", "(Ljava/lang/Object;)Z", false)
                    visitInsn(Opcodes.POP)
                }

                visitInsn(Opcodes.ARETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            visitEnd()
        }.toByteArray()
    }
}