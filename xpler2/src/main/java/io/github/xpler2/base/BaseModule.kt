package io.github.xpler2.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.UnhookParams

abstract class BaseModule : XplerModuleInterface {
    protected val mUnhooks by lazy { mutableListOf<UnhookParams>() }

    override val moduleApplicationId: String?
        get() = null // ASM bytecode injection will be implemented by the xpler2-compiler plugin.

    override val unhooks: List<UnhookParams>
        get() = mUnhooks

    @SuppressWarnings("all")
    @SuppressLint("PrivateApi")
    override fun moduleResources(context: Context): Resources? {
        if (modulePath == null)
            throw IllegalArgumentException("modulePath is null")

        return try {
            val clazz = AssetManager::class.java
            val assetManager = clazz.getConstructor().newInstance()
            val addAssetPathMethod = clazz.getDeclaredMethod("addAssetPath", String::class.java)
                .also { it.isAccessible = true }
            addAssetPathMethod.invoke(assetManager, modulePath)

            Resources(assetManager, context.resources.displayMetrics, context.resources.configuration)
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("PrivateApi")
    override fun injectResource(resources: Resources) {
        if (modulePath == null)
            throw IllegalArgumentException("modulePath is null")

        val method = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            .also { it.isAccessible = true }

        val assets = resources.assets ?: return
        method.invoke(assets, modulePath) // add plugin resources
    }
}