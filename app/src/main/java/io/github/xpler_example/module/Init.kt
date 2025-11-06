package io.github.xpler_example.module

import android.util.Log
import io.github.xpler2.XplerInitialize
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker

@XplerInitialize(
    name = "InitKt",
    description = "Xpler Example Module",
    scope = ["com.tencent.mm"],
    xposed = true,
    lsposed = true,
    lsposedCompatAnnotation = false,
)
fun init(module: XplerModuleInterface) {
    try {
        Log.i("Xpler2", "Hello from Xpler Example Module (Kotlin)!")
        if (!module.isFirstPackage) return // skip if not the first package
        if (module.packageName != "com.tencent.mm") return // skip if not the target package
        if (module.processName.indexOf(":") != -1) return // skip sub-processes
        Log.i("Xpler2", "[Xpler2]Kotlin-> init called in packageName: `${module.packageName}`, process: ${module.processName}")

        val tinkerApplicationClazz = module.classLoader.loadClass("com.tencent.tinker.loader.app.TinkerApplication")
        val onCreateMethod = tinkerApplicationClazz.getDeclaredMethod("onCreate")

        onCreateMethod.hooker {
            onBefore {
                module.log("[Xpler2]Kotlin-> onCreate called in TinkerApplication: $this")
            }

            onAfter {
                module.log("[Xpler2]Kotlin-> TinkerApplication onCreate finished.")
            }
        }
    } catch (e: Throwable) {
        module.log("[Xpler2]Kotlin-> not init.", e)
    }
}