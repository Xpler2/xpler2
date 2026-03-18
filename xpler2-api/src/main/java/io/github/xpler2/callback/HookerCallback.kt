package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.xplerModule

open class HookerCallback {
    val module: XplerModuleInterface
        get() = xplerModule

    open fun onBefore(params: BeforeParams) {
    }

    open fun onAfter(params: AfterParams) {
    }
}