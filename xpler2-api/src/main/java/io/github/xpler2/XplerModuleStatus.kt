package io.github.xpler2

interface XplerModuleStatus {
    companion object {
        private const val STATUS_PROVIDER_CLASS_NAME = "XplerStatusProvider"
        const val UNKNOWN_API_VERSION = -1
        const val UNKNOWN_FRAMEWORK_NAME = "Unknown"
        const val UNKNOWN_FRAMEWORK_VERSION = "Unknown"
        const val UNKNOWN_FRAMEWORK_VERSION_CODE = -1L

        private val provider: XplerModuleStatus by lazy(LazyThreadSafetyMode.PUBLICATION) {
            loadProvider()
        }

        val empty: XplerModuleStatus
            get() = EmptyXplerModuleStatus

        @JvmStatic
        val instance: XplerModuleStatus
            get() = provider

        private fun loadProvider(): XplerModuleStatus {
            return runCatching {
                val classLoader = XplerModuleStatus::class.java.classLoader
                val providerClass = Class.forName(STATUS_PROVIDER_CLASS_NAME, true, classLoader)
                providerClass.getDeclaredField("INSTANCE").get(null) as XplerModuleStatus
            }.getOrDefault(empty)
        }
    }

    /**
     * Whether the module is activated.
     * This is true if the module is activated in the Xposed framework.
     */
    val isActivate: Boolean
        get() = apiVersion != UNKNOWN_API_VERSION

    /**
     * The API version of the framework.
     */
    val apiVersion: Int

    /**
     * The name of the framework.
     */
    val frameworkName: String

    /**
     * The version of the framework.
     */
    val frameworkVersion: String

    /**
     * The version code of the framework.
     */
    val frameworkVersionCode: Long
}

private object EmptyXplerModuleStatus : XplerModuleStatus {
    override val apiVersion: Int
        get() = XplerModuleStatus.UNKNOWN_API_VERSION

    override val frameworkName: String
        get() = XplerModuleStatus.UNKNOWN_FRAMEWORK_NAME

    override val frameworkVersion: String
        get() = XplerModuleStatus.UNKNOWN_FRAMEWORK_VERSION

    override val frameworkVersionCode: Long
        get() = XplerModuleStatus.UNKNOWN_FRAMEWORK_VERSION_CODE
}
