package io.github.xpler_example.module

import android.os.Bundle
import android.util.Log
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker
import io.github.xpler2.hooker.HookerItem
import io.github.xpler2.hooker.IHookerEntity
import io.github.xpler2.params.AfterParams
import io.github.xpler2.params.BeforeParams
import java.lang.reflect.Member

@HookerItem
class KotlinEntity : BaseHookEntity() {
    override val isEnabled: Boolean
        get() = true

    override val priority: Int
        get() = 100

    override fun singleTarget(): Member? {
        try {
            val cls = module.classLoader.loadClass("com.example.app.MainActivity")
            return cls.getMethod("onCreate", Bundle::class.java)
        } catch (t: Throwable) {
            Log.d("Xpler2", "target error: " + t.message, t)
        }
        return null
    }

    override fun onBefore(params: BeforeParams) {
        Log.d("Xpler2", "KotlinEntity onBefore: $params")
    }

    override fun onAfter(params: AfterParams) {
        Log.d("Xpler2", "KotlinEntity onAfter: $params")
    }
}

@HookerItem
class KotlinEntity1 : IHookerEntity {
    override val isEnabled: Boolean
        get() = true

    override fun entrance(module: XplerModuleInterface) {
        val cls = module.classLoader.loadClass("com.example.app.MainActivity")
        val method = cls.getMethod("onCreate", Bundle::class.java)
        method.hooker {
            onBefore {
                Log.d("Xpler2", "KotlinEntity1 onBefore: $this")
            }
        }
    }
}