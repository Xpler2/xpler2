package io.github.xpler2.hooker

import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.callback.HookerCallback
import io.github.xpler2.hooker
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import java.lang.reflect.Constructor
import java.lang.reflect.Method

object HookerEntities {
    private fun registerHooker(entities: Set<HookerEntity>) {
        val module = XplerModuleInterface.instance
        for (entity in entities) {
            if (!entity.isEnabled) continue
            val targets = entity.target() ?: continue

            for (member in targets) {
                if (member == null) continue

                val callback = object : HookerCallback() {
                    override fun onBefore(params: BeforeParams) = entity.onBefore(params)
                    override fun onAfter(params: AfterParams) = entity.onAfter(params)
                }

                try {
                    when (member) {
                        is Constructor<*> -> member.hooker(entity.priority, callback)
                        is Method -> member.hooker(entity.priority, callback)
                        else -> throw IllegalArgumentException(
                            "Unsupported member type: ${member::class.java}. " +
                                    "Only Constructor and Method are supported."
                        )
                    }
                } catch (t: Throwable) {
                    module.log("Failed to register hook for $member: ${t.message}", t)
                }
            }
        }
    }

    fun collect() {
        // ASM logic likely does:
        // registerHooker(setOf(Hooker1(), Hooker2(), ...))
    }
}