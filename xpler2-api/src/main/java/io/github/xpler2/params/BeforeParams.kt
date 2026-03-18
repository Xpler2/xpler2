package io.github.xpler2.params

import java.lang.reflect.Member
import java.lang.reflect.Method

data class BeforeParams(
    private val mMember: () -> Member,
    private val mArgs: () -> Array<Any?>,
    private val mInstance: () -> Any?,
    private val mReturnAndSkip: (Any?) -> Unit,
    private val mThrowAndSkip: (Throwable?) -> Unit,
) {
    private var skipped: Boolean = false

    internal val isSkipped: Boolean
        get() = skipped

    val member get() = mMember.invoke()

    val method get() = mMember.invoke() as Method

    val args get() = mArgs.invoke()

    val instance get() = mInstance.invoke()

    inline fun <reified T> instanceOf(): T = instance as T

    inline fun <reified T> instanceOfOrNull(): T? = instance as? T

    fun returnAndSkip(result: Any?) {
        skipped = true
        mReturnAndSkip.invoke(result)
    }

    fun throwAndSkip(throwable: Throwable?) {
        skipped = true
        mThrowAndSkip.invoke(throwable)
    }

    override fun toString(): String {
        // format json
        // example: {"member": "method", "args": [{"index": 0, "type": "class java.lang.String", "value": "hello"}], "instance": {"type": "class java.lang.String", "value": "hello"}}
        val builder = StringBuilder()
        builder.append("{")
        builder.append("\"member\": \"${member}\"")
        builder.append(", ")

        builder.append("\"args\": [")
        args.forEachIndexed { index, arg ->
            builder.append("{")
            builder.append("\"index\": $index")
            builder.append(", ")
            builder.append("\"type\": \"${arg?.javaClass}\"")
            builder.append(", ")
            builder.append("\"value\": \"${arg?.toString()?.replace("\"", "\\\"")}\"")
            builder.append("}")
            if (index != args.size - 1) {
                builder.append(", ")
            }
        }
        builder.append("]")
        builder.append(", ")

        builder.append("\"instance\": {")
        builder.append("\"type\": \"${instance?.javaClass}\"")
        builder.append(", ")
        builder.append("\"value\": \"${instance?.toString()?.replace("\"", "\\\"")}\"")
        builder.append("}")

        builder.append("}")
        return builder.toString()
    }
}