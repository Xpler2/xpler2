package io.github.xpler2.plugin.asm.method

import io.github.xpler2.plugin.compiler.bean.XplerInitializeBean
import io.github.xpler2.plugin.compiler.bean.XplerInitializeCache
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

// Inject module initialization
class Xpler2ModuleInitMethodVisitor(
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

        // process the `XplerInitialize` annotation, remove the annotation (no bytecode reserved)
        if (annotation.className == "io.github.xpler2.XplerInitialize") {
            generateXposedInitClass()
            generateLsposedInitClass()
            return null
        }

        return super.visitAnnotation(descriptor, visible)
    }

    // generate `XposedInit` class
    private fun generateXposedInitClass() {
        if (!initial.xposed) return

        val className = initial.xposedInit.replace(".", "/")
        val xposed = "io/github/xpler2/base/XposedEntrance"
        ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).apply {
            visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL or Opcodes.ACC_SUPER,
                className,
                null,
                xposed,
                null,
            )

            // constructor
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
            ).apply {
                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    xposed,
                    "<init>",
                    "()V",
                    false
                )
                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            // handleLoadPackage
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "handleLoadPackage",
                "(Lde/robv/android/xposed/callbacks/XC_LoadPackage\$LoadPackageParam;)V",
                null,
                null
            ).apply {
                visitAnnotation("Ljava/lang/Override;", true).apply {
                    visitEnd()
                }

                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    xposed,
                    "handleLoadPackage",
                    "(Lde/robv/android/xposed/callbacks/XC_LoadPackage\$LoadPackageParam;)V",
                    false
                )

                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    className,
                    "getModule",
                    "()Lio/github/xpler2/XplerModuleInterface;",
                    false
                )

                visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    ownerName.replace(".", "/"),
                    methodMame,
                    "(Lio/github/xpler2/XplerModuleInterface;)V",
                    false,
                )

                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            visitEnd()
            writeGenerateClass(className, this)
        }
    }

    // generate `Lsposed` class
    private fun generateLsposedInitClass() {
        if (!initial.lsposed) return

        val className = initial.lsposedInit.replace(".", "/")
        val lsposed = "io/github/xpler2/base/LsposedEntrance"
        ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS).apply {
            visit(
                Opcodes.V17,
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL or Opcodes.ACC_SUPER,
                className,
                null,
                lsposed,
                null,
            )

            // constructor
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(Lio/github/libxposed/api/XposedInterface;Lio/github/libxposed/api/XposedModuleInterface\$ModuleLoadedParam;)V",
                null,
                null
            ).apply {
                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitVarInsn(Opcodes.ALOAD, 2)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    lsposed,
                    "<init>",
                    "(Lio/github/libxposed/api/XposedInterface;Lio/github/libxposed/api/XposedModuleInterface\$ModuleLoadedParam;)V",
                    false
                )
                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            // onPackageLoaded
            visitMethod(
                Opcodes.ACC_PUBLIC,
                "onPackageLoaded",
                "(Lio/github/libxposed/api/XposedModuleInterface\$PackageLoadedParam;)V",
                null,
                null
            ).apply {
                visitAnnotation("Ljava/lang/Override;", true).apply {
                    visitEnd()
                }

                visitCode()
                visitVarInsn(Opcodes.ALOAD, 0)
                visitVarInsn(Opcodes.ALOAD, 1)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    lsposed,
                    "onPackageLoaded",
                    "(Lio/github/libxposed/api/XposedModuleInterface\$PackageLoadedParam;)V",
                    false
                )

                visitVarInsn(Opcodes.ALOAD, 0)
                visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    className,
                    "getModule",
                    "()Lio/github/xpler2/XplerModuleInterface;",
                    false
                )

                visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    ownerName.replace(".", "/"),
                    methodMame,
                    "(Lio/github/xpler2/XplerModuleInterface;)V",
                    false,
                )

                visitInsn(Opcodes.RETURN)
                visitMaxs(0, 0) // ASM 自动计算
                visitEnd()
            }

            visitEnd()
            writeGenerateClass(className, this)
        }
    }

    // write a bytecode file
    private fun writeGenerateClass(name: String, writer: ClassWriter) {
        initializeCache.intermediatesFile(variant)
            .resolve("$name.class")
            .also {
                it.delete()
                it.parentFile.mkdirs()
                println("XplerInitialize: generated init class ${it.absolutePath}")
            }
            .writeBytes(writer.toByteArray())
    }
}