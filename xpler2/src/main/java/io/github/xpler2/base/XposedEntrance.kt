package io.github.xpler2.base

import androidx.annotation.CallSuper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker
import io.github.xpler2.xplerModule

// XposedEntrance
abstract class XposedEntrance : IXposedHookZygoteInit, IXposedHookLoadPackage {
    private lateinit var mModule: XposedModule

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        xplerModule = XposedModule(startupParam)
            .also { mModule = it }
    }

    @CallSuper
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        mModule.mIsFirstPackage = lpparam.isFirstApplication
        mModule.mClassloader = lpparam.classLoader
        mModule.mPackageName = lpparam.packageName
        mModule.mProcessName = lpparam.processName
        initModuleStatus()
    }

    protected val module: XplerModuleInterface
        get() = mModule

    private fun initModuleStatus() {
        if (module.packageName != module.moduleApplicationId) return

        try {
            val statusClazz = module.classLoader.loadClass("io.github.xpler2.base.XposedStatus")
            val getApiVersion = statusClazz?.getDeclaredMethod("getApiVersion")
            if (getApiVersion != null) {
                module.hooker(getApiVersion) {
                    onAfter {
                        result = module.api
                    }
                }
            }
            val getFrameworkName = statusClazz?.getDeclaredMethod("getFrameworkName")
            if (getFrameworkName != null) {
                module.hooker(getFrameworkName) {
                    onAfter {
                        result = module.frameworkName
                    }
                }
            }
        } catch (e: Exception) {
            module.log("[Xpler2] Error initializing Xposed status: ${e.message}", e)
        }
    }
}