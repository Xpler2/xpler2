package io.github.xpler2.params

import java.lang.reflect.Member

data class UnhookParams(
    private val mOrigin: () -> Member,
    private val mUnhook: () -> Unit,
) {
    private var mIsUnhooked = false

    /**
     * Whether the hook has been unhooked.
     */
    val isUnhooked
        get() = mIsUnhooked

    /**
     * The original member that was hooked.
     */
    val member
        get() = mOrigin()

    /**
     * Unhook the method if it has not been unhooked yet.
     */
    fun unhook() {
        if (mIsUnhooked) return
        mUnhook()
        mIsUnhooked = true
    }

    override fun toString(): String {
        // format json: {"isUnhooked": false, "member": "method"}
        val builder = StringBuilder()
        builder.append("{")
        builder.append("\"isUnhooked\": $isUnhooked")
        builder.append(", ")
        builder.append("\"member\": \"${member}\"")
        builder.append("}")
        return builder.toString()
    }
}