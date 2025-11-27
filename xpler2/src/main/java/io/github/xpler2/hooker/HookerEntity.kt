package io.github.xpler2.hooker

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.hooker
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

abstract class HookerEntity : IHookerEntity, HookerCallback() {
    open val priority: Int
        get() = 50

    override fun entrance(module: XplerModuleInterface) {
        val targets = target() ?: return
        for (member in targets) {
            if (member == null) continue

            val callback = object : HookerCallback() {
                override fun onBefore(params: BeforeParams) = this@HookerEntity.onBefore(params)
                override fun onAfter(params: AfterParams) = this@HookerEntity.onAfter(params)
            }

            try {
                when (member) {
                    is Constructor<*> -> member.hooker(priority, callback)
                    is Method -> member.hooker(priority, callback)
                    else -> throw IllegalArgumentException(
                        "Unsupported member type: ${member::class.java}. " +
                                "Only Constructor and Method are supported."
                    )
                }
            } catch (t: Throwable) {
                module.log("Failed to register hook for target $member: ${t.message}", t)
            }
        }
    }

    abstract fun target(): Set<Member?>?
}