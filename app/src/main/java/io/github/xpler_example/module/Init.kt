package io.github.xpler_example.module

import android.util.Log
import io.github.xpler2.XplerInitialize
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker
import io.github.xpler2.hooker.HookerEntities

@XplerInitialize(
    name = $$"$random$",
    description = "Xpler Example Module",
    scope = ["com.example.app"],
    xposed = true,
    lsposed = true,
    lsposedCompatAnnotation = false,
)
fun init(module: XplerModuleInterface) {
    try {
        if (!module.isFirstPackage) return // skip if not the first package
        if (module.packageName != "com.example.app") return // skip if not the target package
        if (module.processName.indexOf(":") != -1) return // skip sub-processes
        Log.i("Xpler2", "[Xpler2]Kotlin-> init called in packageName: `${module.packageName}`, process: ${module.processName}")

        val tinkerApplicationClazz = module.classLoader.loadClass("com.example.app.MyApplication")
        val onCreateMethod = tinkerApplicationClazz.getDeclaredMethod("onCreate")

        onCreateMethod.hooker {
            onBefore {
                module.log("[Xpler2]Kotlin-> onCreate called in Application: $this")
            }

            onAfter {
                module.log("[Xpler2]Kotlin-> Application onCreate finished.")
                HookerEntities.collect()
            }
        }

    } catch (e: Throwable) {
        module.log("[Xpler2]Kotlin-> not init.", e)
    }
}