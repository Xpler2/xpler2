package io.github.xpler2.hooker

import io.github.xpler2.XplerModuleInterface

object HookerEntities {
    private fun registerHooker(entities: Set<IHookerEntity>) {
        val module = XplerModuleInterface.instance
        for (entity in entities) {
            if (!entity.isEnabled) continue

            try {
                entity.entrance(module)
            } catch (t: Throwable) {
                module.log("Failed to register hook for entity $entity: ${t.message}", t)
            }
        }
    }

    fun collect() {
        // ASM logic likely does:
        // registerHooker(setOf(Hooker1(), Hooker2(), ...))
    }
}