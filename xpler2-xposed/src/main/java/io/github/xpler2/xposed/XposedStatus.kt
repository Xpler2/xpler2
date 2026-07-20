package io.github.xpler2.xposed

import io.github.xpler2.XplerModuleStatus

object XposedStatus : XplerModuleStatus {
    override val apiVersion: Int
        get() = XplerModuleStatus.UNKNOWN_API_VERSION

    override val frameworkName: String
        get() = XplerModuleStatus.UNKNOWN_FRAMEWORK_NAME

    override val frameworkVersion: String
        get() = XplerModuleStatus.UNKNOWN_FRAMEWORK_VERSION

    override val frameworkVersionCode: Long
        get() = XplerModuleStatus.UNKNOWN_FRAMEWORK_VERSION_CODE
}
