package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import io.github.xpler2.xplerModule

internal class HookerFunctionImpl() : HookerFunction {
    internal var beforeParamsInner: (BeforeParams.() -> Unit)? = null
        private set

    internal var afterParamsInner: (AfterParams.() -> Unit)? = null
        private set

    override val module: XplerModuleInterface
        get() = xplerModule

    override fun onBefore(params: BeforeParams.() -> Unit) {
        beforeParamsInner = params
    }

    override fun onAfter(params: AfterParams.() -> Unit) {
        afterParamsInner = params
    }
}