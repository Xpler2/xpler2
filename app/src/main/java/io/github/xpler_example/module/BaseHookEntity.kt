package io.github.xpler_example.module

import io.github.xpler2.hooker.HookerEntity
import java.lang.reflect.Member

open class BaseHookEntity : HookerEntity() {
    override fun target(): Set<Member?>? {
        return setOf(singleTarget())
    }

    open fun singleTarget(): Member? {
        return null
    }
}