package io.github.xpler2.callback

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams

interface HookerFunction {
    val module: XplerModuleInterface

    fun onBefore(params: BeforeParams.() -> Unit)

    fun onAfter(params: AfterParams.() -> Unit)

}