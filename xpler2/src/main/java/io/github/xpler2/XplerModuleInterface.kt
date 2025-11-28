package io.github.xpler2

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.os.ParcelFileDescriptor
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.UnhookParams
import java.io.FileNotFoundException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

interface XplerModuleInterface {
    companion object {
        @get:JvmStatic
        val instance: XplerModuleInterface
            get() = xplerModule
    }

    /**
     * Hook method
     * ```
     * class MyHooker : HookerCallback() {
     *   override fun onBefore(params: BeforeParams) {
     *     log("onBefore:params")
     *   }
     *   override fun onAfter(params: AfterParams) {
     *     log("onAfter:params")
     *   }
     * }
     *
     * hooker(Application::class.java.getConstructor(), MyHooker())
     * ```
     */
    fun hooker(
        method: Method,
        callback: HookerCallback,
    ): UnhookParams?

    fun hooker(
        method: Method,
        priority: Int,
        callback: HookerCallback,
    ): UnhookParams?

    /**
     * Hook method
     * ```
     * class MyHooker : HookerCallback() {
     *   override fun onBefore(callback: BeforeParams.() -> Unit) {
     *     log("onBefore:$this")
     *   }
     *   override fun onAfter(callback: AfterParams.() -> Unit) {
     *     log("onAfter:$this")
     *   }
     * }
     *
     * hooker(Application::class.java.getConstructor(), MyHooker())
     * ```
     */
    fun hooker(
        constructor: Constructor<*>,
        callback: HookerCallback,
    ): UnhookParams?

    fun hooker(
        constructor: Constructor<*>,
        priority: Int,
        callback: HookerCallback,
    ): UnhookParams?

    // Get all unhook
    val unhooks: List<UnhookParams>

    /// wrapper
    val api: Int

    val frameworkName: String

    val frameworkVersion: String

    val frameworkVersionCode: Long

    val isFirstPackage: Boolean

    val classLoader: ClassLoader

    val packageName: String

    val processName: String

    val moduleApplicationId: String?

    val modulePath: String?

    fun modulePackageInfo(context: Context): PackageInfo?

    fun moduleResources(resources: Resources): Resources?

    fun injectResource(resources: Resources): Int

    @Throws(UnsupportedOperationException::class)
    fun deoptimize(method: Method): Boolean

    @Throws(UnsupportedOperationException::class)
    fun <T> deoptimize(constructor: Constructor<T>): Boolean

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any?

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?)

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    fun invokeSpecial(method: Method, instance: Any, vararg args: Any?): Any?

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        UnsupportedOperationException::class,
    )
    fun <T> invokeSpecial(method: Constructor<T>, instance: T, vararg args: Any?)

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
        UnsupportedOperationException::class,
    )
    fun <T> newInstanceOrigin(constructor: Constructor<T>, vararg args: Any): T

    @Throws(
        InvocationTargetException::class,
        IllegalArgumentException::class,
        IllegalAccessException::class,
        InstantiationException::class,
        UnsupportedOperationException::class,
    )
    fun <T, U> newInstanceSpecial(constructor: Constructor<T>, subClass: Class<U>, vararg args: Any): U

    @Throws(UnsupportedOperationException::class)
    fun getRemotePreferences(group: String): SharedPreferences

    @Throws(UnsupportedOperationException::class)
    fun listRemoteFiles(): Array<String>

    @Throws(FileNotFoundException::class, UnsupportedOperationException::class)
    fun openRemoteFile(name: String): ParcelFileDescriptor

    fun log(message: String, throwable: Throwable?)

    fun log(message: String)

    fun logStackTraceString()

    fun stackTraceString(): String
}