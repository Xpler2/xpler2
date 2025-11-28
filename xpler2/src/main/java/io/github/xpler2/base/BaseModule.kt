package io.github.xpler2.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.params.UnhookParams
import java.io.File

abstract class BaseModule : XplerModuleInterface {
    protected val mUnhooks by lazy { mutableListOf<UnhookParams>() }

    override val moduleApplicationId: String?
        get() = null // ASM bytecode injection will be implemented by the xpler2-compiler plugin.

    override val unhooks: List<UnhookParams>
        get() = mUnhooks

    override fun modulePackageInfo(context: Context): PackageInfo? {
        val moduleFile = modulePath?.let { File(it) } ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val flags = PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
            context.packageManager.getPackageArchiveInfo(moduleFile.absolutePath, flags)
        } else {
            context.packageManager.getPackageArchiveInfo(
                moduleFile.absolutePath,
                PackageManager.GET_ACTIVITIES
            )
        }
    }

    @SuppressWarnings("all")
    @SuppressLint("PrivateApi")
    override fun moduleResources(resources: Resources): Resources? {
        if (modulePath == null)
            throw IllegalArgumentException("modulePath is null")

        return try {
            val clazz = AssetManager::class.java
            val assetManager = clazz.getConstructor().newInstance()
            val addAssetPathMethod = clazz.getDeclaredMethod("addAssetPath", String::class.java)
                .also { it.isAccessible = true }
            addAssetPathMethod.invoke(assetManager, modulePath)

            Resources(
                assetManager,
                resources.displayMetrics,
                resources.configuration
            )
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("PrivateApi")
    override fun injectResource(resources: Resources): Int {
        if (modulePath == null)
            throw IllegalArgumentException("modulePath is null")

        val method = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
            .also { it.isAccessible = true }

        val assets = resources.assets ?: return -1
        return method.invoke(assets, modulePath) as? Int ?: -1 // add plugin resources
    }
}