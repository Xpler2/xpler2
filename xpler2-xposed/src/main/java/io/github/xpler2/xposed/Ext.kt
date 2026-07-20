package io.github.xpler2.xposed

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.callback.HookerFunction

val XplerModuleInterface.asXposed: XposedModule?
    get() = this as? XposedModule

inline fun XplerModuleInterface.withXposed(block: XposedModule.() -> Unit) {
    val xposedModule = asXposed ?: error("Current module is not XposedModule: ${this::class.java.name}")
    block(xposedModule)
}

inline fun HookerCallback.withXposed(block: XposedModule.() -> Unit) {
    val xposedModule = module.asXposed ?: error("Current module is not XposedModule: ${this::class.java.name}")
    block(xposedModule)
}

inline fun HookerFunction.withXposed(block: XposedModule.() -> Unit) {
    val xposedModule = module.asXposed ?: error("Current module is not XposedModule: ${this::class.java.name}")
    block(xposedModule)
}