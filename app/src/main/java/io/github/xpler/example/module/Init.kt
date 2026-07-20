package io.github.xpler.example.module

import android.os.Bundle
import android.util.Log
import io.github.xpler2.XplerHint
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker
import io.github.xpler2.xposed.XposedHint

@XplerHint(
    description = "Xpler Example Module",
    scope = ["io.abc.a116"],
)
@XposedHint(version = 82)
// @LsposedHint(version = 100) // ~! perhaps it will be supported in the future
fun init(module: XplerModuleInterface) {
    try {
        if (!module.isFirstPackage) return // skip if not the first package
        if (module.packageName != "io.abc.a116") return // skip if not the target package
        if (module.processName.indexOf(":") != -1) return // skip sub-processes
        Log.i("Xpler2", "[Xpler2]init called in packageName: `${module.packageName}`, process: ${module.processName}")

        val applicationClazz = module.classLoader.loadClass("io.abc.a116.MainActivity")
        val onCreateMethod = applicationClazz.getDeclaredMethod("onCreate", Bundle::class.java)

        onCreateMethod.hooker {
            onBefore {
                module.log("[Xpler2]before[50]: $this")
                returnAndSkip(null)
            }

            onAfter {
                module.log("[Xpler2]after[50]: $this")
            }
        }

        onCreateMethod.hooker(51) {
            onBefore {
                module.log("[Xpler2]before[51]: $this")
            }

            onAfter {
                module.log("[Xpler2]after[51]: $this")
            }
        }

        onCreateMethod.hooker(49) {
            onBefore {
                module.log("[Xpler2]before[49]: $this")
            }

            onAfter {
                module.log("[Xpler2]after[49]: $this")
            }
        }

    } catch (e: Throwable) {
        module.log("[Xpler2]not init.", e)
    }
}