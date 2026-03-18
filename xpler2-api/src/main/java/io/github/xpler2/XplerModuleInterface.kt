package io.github.xpler2

import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.Resources
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Constructor
import java.lang.reflect.Method

interface XplerModuleInterface {
    val api: Int

    val frameworkName: String

    val frameworkVersion: String

    val frameworkVersionCode: Long

    val isFirstPackage: Boolean

    val classLoader: ClassLoader

    val packageName: String

    val processName: String

    val moduleApplicationId: String

    val modulePath: String

    fun invokeOrigin(method: Method, instance: Any, vararg args: Any?): Any?

    fun <T> invokeOrigin(constructor: Constructor<T>, instance: T, vararg args: Any?): Any?

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

    fun modulePackageInfo(context: Context): PackageInfo?

    fun moduleResources(context: Context): Resources?

    fun injectResource(resources: Resources): Int

    fun log(message: String, throwable: Throwable?)

    fun log(message: String)

    fun getStackTraceString(): String
}