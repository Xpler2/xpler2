package io.github.xpler2.hooker

import io.github.xpler2.XplerModuleInterface

interface IHookerEntity {
    val isEnabled: Boolean

    fun entrance(module: XplerModuleInterface)
}