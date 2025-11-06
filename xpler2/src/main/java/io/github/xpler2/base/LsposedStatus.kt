package io.github.xpler2.base

import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import io.github.xpler2.XplerModuleStatus

object LsposedStatus : XplerModuleStatus {
    private var mXposedService: XposedService? = null

    init {
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                mXposedService = service
            }

            override fun onServiceDied(service: XposedService) {
                mXposedService = service
            }
        })
    }

    override val isActivate: Boolean
        get() = apiVersion != -1

    override val apiVersion: Int
        get() = mXposedService?.apiVersion ?: -1

    override val frameworkVersion
        get() = mXposedService?.frameworkVersion ?: "Unknown"

    override val frameworkVersionCode
        get() = mXposedService?.frameworkVersionCode ?: -1L

    override val frameworkName
        get() = mXposedService?.frameworkName ?: "Unknown"

    override val scope
        get() = mXposedService?.scope ?: emptyList()

    override fun removeScope(packageName: String): String? {
        return mXposedService?.removeScope(packageName)
    }

    override fun requestScope(packageName: String) {
        mXposedService?.requestScope(packageName, object : XposedService.OnScopeEventListener {
            override fun onScopeRequestPrompted(packageName: String) {

            }

            override fun onScopeRequestApproved(packageName: String) {

            }

            override fun onScopeRequestDenied(packageName: String) {

            }

            override fun onScopeRequestTimeout(packageName: String) {

            }

            override fun onScopeRequestFailed(packageName: String, message: String) {

            }
        })
    }

    override fun getRemotePreferences(group: String) {
        mXposedService?.getRemotePreferences(group)
    }

    override fun deleteRemotePreferences(group: String) {
        mXposedService?.deleteRemotePreferences(group)
    }

    override fun listRemoteFiles(): Array<String> {
        return mXposedService?.listRemoteFiles() ?: emptyArray()
    }

    override fun openRemoteFile(name: String) {
        mXposedService?.openRemoteFile(name)
    }

    override fun deleteRemoteFile(name: String) {
        mXposedService?.deleteRemoteFile(name)
    }
}