package io.github.xpler2

import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.callback.HookerFunction
import io.github.xpler2.callback.HookerFunctionImpl
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.params.UnhookParams
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal lateinit var xplerModule: XplerModuleInterface

fun XplerModuleInterface.hooker(method: Method, callback: HookerFunction.() -> Unit): UnhookParams? {
    val impl = HookerFunctionImpl().apply(callback)
    return xplerModule.hooker(method, object : HookerCallback() {
        override fun onBefore(params: BeforeParams) {
            impl.beforeParamsInner?.invoke(params)
        }

        override fun onAfter(params: AfterParams) {
            impl.afterParamsInner?.invoke(params)
        }
    })
}

fun XplerModuleInterface.hooker(method: Method, priority: Int, callback: HookerFunction.() -> Unit): UnhookParams? {
    val impl = HookerFunctionImpl().apply(callback)
    return xplerModule.hooker(method, priority, object : HookerCallback() {
        override fun onBefore(params: BeforeParams) {
            impl.beforeParamsInner?.invoke(params)
        }

        override fun onAfter(params: AfterParams) {
            impl.afterParamsInner?.invoke(params)
        }
    })
}

fun XplerModuleInterface.hooker(constructor: Constructor<*>, callback: HookerFunction.() -> Unit): UnhookParams? {
    val impl = HookerFunctionImpl().apply(callback)
    return xplerModule.hooker(constructor, object : HookerCallback() {
        override fun onBefore(params: BeforeParams) {
            impl.beforeParamsInner?.invoke(params)
        }

        override fun onAfter(params: AfterParams) {
            impl.afterParamsInner?.invoke(params)
        }
    })
}

fun XplerModuleInterface.hooker(constructor: Constructor<*>, priority: Int, callback: HookerFunction.() -> Unit): UnhookParams? {
    val impl = HookerFunctionImpl().apply(callback)
    return xplerModule.hooker(constructor, priority, object : HookerCallback() {
        override fun onBefore(params: BeforeParams) {
            impl.beforeParamsInner?.invoke(params)
        }

        override fun onAfter(params: AfterParams) {
            impl.afterParamsInner?.invoke(params)
        }
    })
}

/**
 * Hook method
 * ```
 * class MyHooker : HookerCallback() {
 *   override fun onBefore(params: BeforeParams) {
 *     log("onBefore:$params")
 *   }
 *   override fun onAfter(params: AfterParams) {
 *     log("onAfter:$params")
 *   }
 * }
 *
 * Application::class.java.getDeclaredMethod("attach", Context::class.java).hooker(MyHooker())
 * ```
 * @param callback callback
 */
fun Method.hooker(callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Method.hooker(priority: Int, callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook method
 * ```
 * Application::class.java.getDeclaredMethod("attach", Context::class.java).hooker {
 *    onBefore {
 *      log("onBefore:$this")
 *    }
 *    onAfter {
 *      log("onAfter:$this")
 *    }
 * }
 * ```
 * @param callback callback
 */
fun Method.hooker(callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Method.hooker(priority: Int, callback: HookerFunction.() -> Unit): UnhookParams? {
    val impl = HookerFunctionImpl().apply(callback)
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook method
 * ```
 * class MyHooker : HookerCallback() {
 *   override fun onBefore(params: BeforeParams) {
 *     log("onBefore:$params")
 *   }
 *   override fun onAfter(params: AfterParams) {
 *     log("onAfter:$params")
 *   }
 * }
 *
 * Application::class.java.getConstructor().hooker(MyHooker())
 *```
 * @param callback callback
 */
fun Constructor<*>.hooker(callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Constructor<*>.hooker(priority: Int, callback: HookerCallback): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook constructor
 * ```
 * Application::class.java.getConstructor().hooker{
 *    onBefore {
 *      log("onBefore:$this")
 *    }
 *    onAfter {
 *      log("onAfter:$this")
 *    }
 * }
 * ```
 * @param callback callback
 */
fun Constructor<*>.hooker(callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, callback)
}

fun Constructor<*>.hooker(priority: Int, callback: HookerFunction.() -> Unit): UnhookParams? {
    return xplerModule.hooker(this, priority, callback)
}

/**
 * Hook all methods
 * ```
 * Application::class.java.hookerMethodAll {
 *   onBefore {
 *     log("onBefore:$this")
 *   }
 *   onAfter {
 *     log("onAfter:$this")
 *   }
 * }
 * ```
 * @param callback callback
 */
fun Class<*>.hookerMethodAll(callback: HookerFunction.() -> Unit): List<UnhookParams?> {
    return declaredMethods
        .map { m ->
            if (Modifier.isAbstract(m.modifiers)) return@map null
            xplerModule.hooker(m, callback)
        }
}

/**
 * Hook all constructors
 * ```
 * Application::class.java.hookerConstructorAll {
 *   onBefore {
 *     log("onBefore:$this")
 *   }
 *   onAfter {
 *     log("onAfter:$this")
 *   }
 * }
 * ```
 * @param callback callback
 */
fun Class<*>.hookerConstructorAll(callback: HookerFunction.() -> Unit): List<UnhookParams?> {
    return declaredConstructors
        .map { c ->
            xplerModule.hooker(c, callback)
        }
}