package io.github.xpler_example.module

import android.util.Log
import io.github.xpler2.XplerInitialize
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker
import io.github.xpler2.hookerMethodAll

// @XplerInitialize(
//     name = $$"$random$",
//     description = "Xpler Example Module",
//     scope = ["com.example.app"],
//     xposed = true,
//     lsposed = false,
//     lsposedCompatAnnotation = false,
// )
fun init(module: XplerModuleInterface) {
    try {
        if (!module.isFirstPackage) return // skip if not the first package
        if (module.packageName != "com.example.app") return // skip if not the target package
        if (module.processName.indexOf(":") != -1) return // skip sub-processes
        Log.i("Xpler2", "[Xpler2]Kotlin-> init called in packageName: `${module.packageName}`, process: ${module.processName}")

        val applicationClazz = module.classLoader.loadClass("com.example.app.MyApplication")
        val onCreateMethod = applicationClazz.getDeclaredMethod("onCreate")
        onCreateMethod.hooker {
            onBefore {
                module.log("[Xpler2]Kotlin-> onCreate called in Application: $this")
            }

            onAfter {
                module.log("[Xpler2]Kotlin-> Application onCreate finished.")
            }
        }
    } catch (e: Throwable) {
        module.log("[Xpler2]Kotlin-> not init.", e)
    }
}

@XplerInitialize(
    name = $$"$random$",
    description = "Xpler Example Module",
    scope = ["com.engeling.bitcontrol"],
    xposed = true,
    lsposed = false,
    lsposedCompatAnnotation = false,
)
fun init1(module: XplerModuleInterface) {
    try {
        if (!module.isFirstPackage) return // skip if not the first package
        if (module.packageName != "com.engeling.bitcontrol") return // skip if not the target package
        if (module.processName.indexOf(":") != -1) return // skip sub-processes
        Log.i("Xpler2", "[Xpler2]Kotlin-> init called in packageName: `${module.packageName}`, process: ${module.processName}")

        val cls = module.classLoader.loadClass("w0.d")
        val onResumeMethod = cls.getDeclaredMethod("onResume")
        cls.hookerMethodAll {
            onBefore {
                module.log("[Xpler2]Kotlin-> onResume called: $this")
            }

            onAfter {
                module.log("[Xpler2]Kotlin-> onResume finished.")
            }
        }
    } catch (e: Throwable) {
        module.log("[Xpler2]Kotlin-> not init.", e)
    }
}