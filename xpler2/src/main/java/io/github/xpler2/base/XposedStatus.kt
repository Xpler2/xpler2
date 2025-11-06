package io.github.xpler2.base

import io.github.xpler2.XplerModuleStatus

object XposedStatus : XplerModuleStatus {
    override val isActivate: Boolean
        get() = apiVersion != -1

    override val apiVersion: Int
        get() = -1

    override val frameworkName: String
        get() = "Unknown"

    override val frameworkVersion: String
        get() = "Unknown"

    override val frameworkVersionCode: Long
        get() = -1L

    override val scope: List<String>
        get() = throw UnsupportedOperationException("current xposed api does not support `getScope`")

    override fun removeScope(packageName: String): String? {
        throw UnsupportedOperationException("current xposed api does not support `removeScope`")
    }

    override fun requestScope(packageName: String) {
        throw UnsupportedOperationException("current xposed api does not support `requestScope`")
    }

    override fun getRemotePreferences(group: String) {
        throw UnsupportedOperationException("current xposed api does not support `getRemotePreferences`")
    }

    override fun deleteRemotePreferences(group: String) {
        throw UnsupportedOperationException("current xposed api does not support `deleteRemotePreferences`")
    }

    override fun listRemoteFiles(): Array<String> {
        throw UnsupportedOperationException("current xposed api does not support `listRemoteFiles`")
    }

    override fun openRemoteFile(name: String) {
        throw UnsupportedOperationException("current xposed api does not support `openRemoteFile`")
    }

    override fun deleteRemoteFile(name: String) {
        throw UnsupportedOperationException("current xposed api does not support `deleteRemoteFile`")
    }
}