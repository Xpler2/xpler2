package io.xpler2.github.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.annotation.CallSuper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xpler2.HookerFactory
import io.github.xpler2.HookerStore
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.hooker
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import io.github.xpler2.xplerModule
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Member
import java.lang.reflect.Method

abstract class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage, XplerModuleInterface {
    private lateinit var mStartupParam: IXposedHookZygoteInit.StartupParam

    private var mIsFirstPackage: Boolean = false
    private lateinit var mClassloader: ClassLoader
    private lateinit var mPackageName: String
    private lateinit var mProcessName: String

    init {
        xplerModule = this
    }

    @CallSuper
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        mStartupParam = startupParam
    }

    @CallSuper
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        mIsFirstPackage = lpparam.isFirstApplication
        mClassloader = lpparam.classLoader
        mPackageName = lpparam.packageName
        mProcessName = lpparam.processName
        initialization()
    }

    private fun initialization() {
        if (packageName != moduleApplicationId) return

        try {
            val statusClazz = classLoader.loadClass("io.xpler2.github.xposed.XposedStatus")

            // getApiVersion
            statusClazz
                ?.getDeclaredMethod("getApiVersion")
                ?.hooker {
                    onAfter {
                        result = module.api
                    }
                }

            // getFrameworkName
            statusClazz
                ?.getDeclaredMethod("getFrameworkName")
                ?.hooker {
                    onAfter {
                        result = module.frameworkName
                    }
                }
        } catch (e: Exception) {
            log("[Xpler2] Error initializing Xposed: ${e.message}", e)
        }
    }

    override val api: Int
        get() = XposedBridge.getXposedVersion()

    override val frameworkName: String
        get() {
            val declaredField = XposedBridge::class.java.getDeclaredField("TAG")
            declaredField.isAccessible = true
            return "${declaredField.get(null) ?: "Unknown"}"
        }

    override val frameworkVersion: String
        get() = "Unknown"

    override val frameworkVersionCode: Long
        get() = -1

    override val isFirstPackage: Boolean
        get() = mIsFirstPackage

    override val classLoader: ClassLoader
        get() = mClassloader

    override val packageName: String
        get() = mPackageName

    override val processName: String
        get() = mProcessName

    override val modulePath: String
        get() = mStartupParam.modulePath

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(method, instance, args)
    }

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
    )
    override fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(constructor, instance, args)
    }

    //
    override fun hooker(
        method: Method,
        callback: HookerCallback
    ) = buildXposedHookerCallbackImpl(method, XC_MethodHook.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback
    ) = buildXposedHookerCallbackImpl(method, priority, callback)

    override fun hooker(
        constructor: Constructor<*>,
        callback: HookerCallback
    ) = buildXposedHookerCallbackImpl(constructor, XC_MethodHook.PRIORITY_DEFAULT, callback)

    override fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback
    ) = buildXposedHookerCallbackImpl(constructor, priority, callback)

    override fun modulePackageInfo(context: Context): PackageInfo? {
        val moduleFile = File(modulePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val flags = PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
            context.packageManager.getPackageArchiveInfo(moduleFile.absolutePath, flags)
        } else {
            context.packageManager.getPackageArchiveInfo(
                moduleFile.absolutePath,
                PackageManager.GET_ACTIVITIES
            )
        }
    }

    @SuppressWarnings("all")
    @SuppressLint("PrivateApi")
    override fun moduleResources(context: Context): Resources? {
        return try {
            val clazz = AssetManager::class.java
            val assetManager = clazz.getConstructor().newInstance()
            val addAssetPathMethod = clazz.getDeclaredMethod("addAssetPath", String::class.java)
                .also { it.isAccessible = true }
            addAssetPathMethod.invoke(assetManager, modulePath)

            Resources(
                assetManager,
                context.resources.displayMetrics,
                context.resources.configuration
            )
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("PrivateApi")
    override fun injectResource(resources: Resources): Int {
        val method = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            .also { it.isAccessible = true }

        val assets = resources.assets ?: return -1
        return method.invoke(assets, modulePath) as? Int ?: -1 // add plugin resources
    }

    override fun log(message: String, throwable: Throwable?) {
        if (throwable != null)
            XposedBridge.log(Exception(message, throwable))
        else
            XposedBridge.log(message)
    }

    override fun log(message: String) = log(message, null)

    override fun getStackTraceString() = Log.getStackTraceString(RuntimeException("stackTraceString"))

    //
    private class Hooker(priority: Int) : XC_MethodHook(priority) {
        private val skippedStates = ThreadLocal
            .withInitial { mutableMapOf<Member, ArrayDeque<Boolean>>() }

        override fun beforeHookedMethod(param: MethodHookParam) {
            val member = param.method
            pushSkippedState(member)
            HookerStore.get(member)?.dispatchBefore(
                BeforeParams(
                    mMember = { member },
                    mArgs = { param.args },
                    mInstance = { param.thisObject },
                    mReturnAndSkip = {
                        markSkipped(member)
                        param.result = it
                    },
                    mThrowAndSkip = {
                        markSkipped(member)
                        param.throwable = it
                    },
                )
            )
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            val member = param.method
            try {
                HookerStore.get(member)?.dispatchAfter(
                    AfterParams(
                        mMember = { member },
                        mArgs = { param.args },
                        mInstance = { param.thisObject },
                        mResult = { param.result },
                        mThrowable = { param.throwable },
                        mIsSkipped = { isSkipped(member) },
                        mSetResult = { param.result = it },
                        mSetThrowable = { param.throwable = it }
                    )
                )
            } finally {
                popSkippedState(member)
            }
        }

        private fun pushSkippedState(member: Member) {
            val states = currentSkippedStates()
            val stack = states.getOrPut(member) { ArrayDeque() }
            stack.addLast(false)
        }

        private fun markSkipped(member: Member) {
            val states = currentSkippedStates()
            val stack = states.getOrPut(member) { ArrayDeque() }
            if (stack.isEmpty()) {
                stack.addLast(true)
                return
            }
            stack.removeLast()
            stack.addLast(true)
        }

        private fun isSkipped(member: Member): Boolean {
            return currentSkippedStates()[member]?.lastOrNull() == true
        }

        private fun popSkippedState(member: Member) {
            val states = currentSkippedStates()
            val stack = states[member] ?: return
            if (stack.isNotEmpty()) {
                stack.removeLast()
            }
            if (stack.isEmpty()) {
                states.remove(member)
            }
            if (states.isEmpty()) {
                skippedStates.remove()
            }
        }

        private fun currentSkippedStates(): MutableMap<Member, ArrayDeque<Boolean>> {
            return skippedStates.get() ?: mutableMapOf<Member, ArrayDeque<Boolean>>().also {
                skippedStates.set(it)
            }
        }
    }

    private fun buildXposedHookerCallbackImpl(
        target: Member,
        priority: Int, // PRIORITY_DEFAULT = 50
        callback: HookerCallback,
    ): UnhookParams {
        while (true) {
            val factory = HookerStore.computeIfAbsent(target) {
                HookerFactory(target) {
                    val unhookOriginal = when (target) {
                        is Method -> XposedBridge.hookMethod(target, Hooker(priority))
                        is Constructor<*> -> XposedBridge.hookMethod(target, Hooker(priority))
                        else -> throw IllegalArgumentException("Unsupported member type: $target")
                    }
                    UnhookParams(
                        mOrigin = { unhookOriginal.hookedMethod },
                        mUnhook = { unhookOriginal.unhook() },
                    )
                }
            }

            factory.register(priority, callback) {
                HookerStore.remove(target, factory)
            }?.let {
                return it
            }

            HookerStore.remove(target, factory)
        }
    }
}
