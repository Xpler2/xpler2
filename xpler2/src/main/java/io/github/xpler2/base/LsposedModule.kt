package io.github.xpler2.base

import android.content.SharedPreferences
import android.os.ParcelFileDescriptor
import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.annotations.AfterInvocation
import io.github.libxposed.api.annotations.BeforeInvocation
import io.github.libxposed.api.annotations.XposedHooker
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

internal class LsposedModule(
    private val mXposedInterface: XposedInterface,
) : BaseModule() {
    var mIsFirstPackage: Boolean = false
    lateinit var mClassloader: ClassLoader
    lateinit var mPackageName: String
    lateinit var mProcessName: String

    @XposedHooker
    object Hooker : XposedInterface.Hooker {
        @JvmStatic
        @BeforeInvocation
        fun before(callback: XposedInterface.BeforeHookCallback) {
            HookerStore.hookers[callback.member]?.dispatchBefore(
                BeforeParams(
                    mMember = { callback.member },
                    mArgs = { callback.args },
                    mInstance = { callback.thisObject },
                    mReturnAndSkip = { callback.returnAndSkip(it) },
                    mThrowAndSkip = { callback.throwAndSkip(it) },
                )
            )
        }

        @JvmStatic
        @AfterInvocation
        fun after(callback: XposedInterface.AfterHookCallback) {
            HookerStore.hookers[callback.member]?.dispatchAfter(
                AfterParams(
                    mMember = { callback.member },
                    mArgs = { callback.args },
                    mInstance = { callback.thisObject },
                    mResult = { callback.result },
                    mThrowable = { callback.throwable },
                    mIsSkipped = { callback.isSkipped },
                    mSetResult = { callback.result = it },
                    mSetThrowable = { callback.throwable = it }
                )
            )
        }
    }

    private fun <T : Member> buildLsposedHookerCallbackImpl(
        target: T,
        priority: Int, // PRIORITY_DEFAULT = 50
        callback: HookerCallback,
    ): UnhookParams? {
        val factory = HookerStore.computeIfAbsent(target, HookerFactory(target) {
            val unhookOriginal = when (target) {
                is Method -> mXposedInterface.hook(target, priority, Hooker::class.java)
                is Constructor<*> -> mXposedInterface.hook(target, priority, Hooker::class.java)
                else -> throw IllegalArgumentException("Unsupported member type: $target")
            }
            UnhookParams(
                mOrigin = { unhookOriginal.origin },
                mUnhook = { unhookOriginal.unhook() },
            )
        })

        return factory.register(priority, callback) { if (factory.isEmpty()) HookerStore.remove(target) }
            .also { mUnhooks.add(it) }
    }

    override fun hooker(
        method: Method,
        callback: HookerCallback,
    ) = buildLsposedHookerCallbackImpl(method, XposedInterface.PRIORITY_DEFAULT, callback)

    override fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback,
    ) = buildLsposedHookerCallbackImpl(method, priority, callback)

    override fun hooker(
        constructor: Constructor<*>,
        callback: HookerCallback,
    ) = buildLsposedHookerCallbackImpl(constructor, XposedInterface.PRIORITY_DEFAULT, callback)

    override fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback,
    ) = buildLsposedHookerCallbackImpl(constructor, priority, callback)

    override val api: Int
        get() {
            val declaredField = XposedInterface::class.java.getDeclaredField("API") ?: return -1
            declaredField.isAccessible = true
            return declaredField.getInt(null)
        }

    override val frameworkName: String
        get() = mXposedInterface.frameworkName

    override val frameworkVersion: String
        get() = mXposedInterface.frameworkVersion

    override val frameworkVersionCode: Long
        get() = mXposedInterface.frameworkVersionCode

    override val isFirstPackage: Boolean
        get() = mIsFirstPackage

    override val classLoader: ClassLoader
        get() = mClassloader

    override val packageName: String
        get() = mPackageName

    override val processName: String
        get() = mProcessName

    override val modulePath: String?
        get() = mXposedInterface.applicationInfo.sourceDir

    override fun deoptimize(method: Method): Boolean {
        return mXposedInterface.deoptimize(method)
    }

    override fun <T> deoptimize(constructor: Constructor<T>): Boolean {
        return mXposedInterface.deoptimize(constructor)
    }

    override fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any? {
        return mXposedInterface.invokeOrigin(method, instance, *args)
    }

    override fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?) {
        instance ?: throw IllegalArgumentException("instance is null")
        mXposedInterface.invokeOrigin(constructor, instance, *args)
    }

    override fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any? {
        return mXposedInterface.invokeSpecial(method, instance, *args)
    }

    override fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?) {
        instance ?: throw IllegalArgumentException("instance is null")
        mXposedInterface.invokeSpecial(method, instance, *args)
    }

    override fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T {
        return mXposedInterface.newInstanceOrigin(constructor, *args)
    }

    override fun <T, U> newInstanceSpecial(constructor: Constructor<T>, subClass: Class<U>, vararg args: Any): U {
        return mXposedInterface.newInstanceSpecial(constructor, subClass, *args)
    }

    override fun getRemotePreferences(group: String): SharedPreferences {
        return mXposedInterface.getRemotePreferences(group)
    }

    override fun listRemoteFiles(): Array<String> {
        return mXposedInterface.listRemoteFiles()
    }

    override fun openRemoteFile(name: String): ParcelFileDescriptor {
        return mXposedInterface.openRemoteFile(name)
    }

    override fun log(message: String, throwable: Throwable?) {
        if (throwable != null)
            mXposedInterface.log(message, throwable)
        else
            mXposedInterface.log(message)
    }

    override fun log(message: String) = log(message, null)

    override fun logStackTraceString() = log(stackTraceString())

    override fun stackTraceString(): String {
        return Log.getStackTraceString(RuntimeException("stackTraceString"))
    }
}