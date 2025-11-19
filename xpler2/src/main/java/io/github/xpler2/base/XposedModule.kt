package io.github.xpler2.base

import android.content.SharedPreferences
import android.os.ParcelFileDescriptor
import android.util.Log
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

internal class XposedModule(
    private val mStartupParam: IXposedHookZygoteInit.StartupParam,
) : BaseModule() {
    var mIsFirstPackage: Boolean = false
    lateinit var mClassloader: ClassLoader
    lateinit var mPackageName: String
    lateinit var mProcessName: String

    class Hooker(priority: Int) : XC_MethodHook(priority) {
        private val isSkipped = ThreadLocal
            .withInitial { mutableMapOf<Member, Boolean>() }

        override fun beforeHookedMethod(param: MethodHookParam) {
            HookerStore.hookers[param.method]?.dispatchBefore(
                BeforeParams(
                    mMember = { param.method },
                    mArgs = { param.args },
                    mInstance = { param.thisObject },
                    mReturnAndSkip = {
                        isSkipped.get()?.put(param.method, true)
                        param.result = it
                    },
                    mThrowAndSkip = {
                        isSkipped.get()?.put(param.method, true)
                        param.throwable = it
                    },
                )
            )
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            HookerStore.hookers[param.method]?.dispatchAfter(
                AfterParams(
                    mMember = { param.method },
                    mArgs = { param.args },
                    mInstance = { param.thisObject },
                    mResult = { param.result },
                    mThrowable = { param.throwable },
                    mIsSkipped = { isSkipped.get()?.get(param.method) == true },
                    mSetResult = { param.result = it },
                    mSetThrowable = { param.throwable = it }
                )
            )
        }
    }

    private fun buildXposedHookerCallbackImpl(
        target: Member,
        priority: Int, // PRIORITY_DEFAULT = 50
        callback: HookerCallback,
    ): UnhookParams? {
        val factory = HookerStore.computeIfAbsent(target, HookerFactory(target) {
            val unhookOriginal = when (target) {
                is Method -> XposedBridge.hookMethod(target, Hooker(priority))
                is Constructor<*> -> XposedBridge.hookMethod(target, Hooker(priority))
                else -> throw IllegalArgumentException("Unsupported member type: $target")
            }
            UnhookParams(
                mOrigin = { unhookOriginal.hookedMethod },
                mUnhook = { unhookOriginal.unhook() },
            )
        })

        return factory.register(priority, callback) { if (factory.isEmpty()) HookerStore.remove(target) }
            .also { mUnhooks.add(it) }
    }

    override fun hooker(
        method: Method,
        callback: HookerCallback,
    ) = buildXposedHookerCallbackImpl(method, XC_MethodHook.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback,
    ) = buildXposedHookerCallbackImpl(method, priority, callback)

    override fun hooker(
        constructor: Constructor<*>,
        callback: HookerCallback,
    ) = buildXposedHookerCallbackImpl(constructor, XC_MethodHook.PRIORITY_DEFAULT, callback)

    override fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback,
    ) = buildXposedHookerCallbackImpl(constructor, priority, callback)

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

    override val modulePath: String?
        get() = mStartupParam.modulePath

    override fun deoptimize(method: Method): Boolean {
        throw UnsupportedOperationException("current xposed api does not support `deoptimize`")
    }

    override fun <T> deoptimize(constructor: Constructor<T>): Boolean {
        throw UnsupportedOperationException("current xposed api does not support `deoptimize`")
    }

    override fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any? {
        return XposedBridge.invokeOriginalMethod(method, instance, args)
    }

    override fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?) {
        XposedBridge.invokeOriginalMethod(constructor, instance, args)
    }

    override fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any? {
        throw UnsupportedOperationException("current xposed api does not support `invokeSpecial`")
    }

    override fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?) {
        throw UnsupportedOperationException("current xposed api does not support `invokeSpecial`")
    }

    override fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T {
        throw UnsupportedOperationException("current xposed api does not support `newInstanceOrigin`")
    }

    override fun <T, U> newInstanceSpecial(constructor: Constructor<T>, subClass: Class<U>, vararg args: Any): U {
        throw UnsupportedOperationException("current xposed api does not support `newInstanceSpecial`")
    }

    override fun getRemotePreferences(group: String): SharedPreferences {
        throw UnsupportedOperationException("current xposed api does not support `getRemotePreferences`")
    }

    override fun listRemoteFiles(): Array<String> {
        throw UnsupportedOperationException("current xposed api does not support `listRemoteFiles`")
    }

    override fun openRemoteFile(name: String): ParcelFileDescriptor {
        throw UnsupportedOperationException("current xposed api does not support `openRemoteFile`")
    }

    override fun log(message: String, throwable: Throwable?) {
        if (throwable != null)
            XposedBridge.log(Exception(message, throwable))
        else
            XposedBridge.log(message)
    }

    override fun log(message: String) = log(message, null)

    override fun logStackTraceString() = log(stackTraceString())

    override fun stackTraceString(): String {
        return Log.getStackTraceString(RuntimeException("stackTraceString"))
    }
}