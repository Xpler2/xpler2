package io.github.xpler2.hooker

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.xplerModule
import java.lang.reflect.Member

abstract class HookerEntity {
    val module: XplerModuleInterface
        get() = xplerModule

    open val isEnabled: Boolean
        get() = true

    open val priority: Int
        get() = 50

    abstract fun target(): Set<Member?>?

    open fun onBefore(params: BeforeParams) {

    }

    open fun onAfter(params: AfterParams) {

    }
}