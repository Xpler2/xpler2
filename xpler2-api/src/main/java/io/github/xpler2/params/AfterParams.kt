package io.github.xpler2.params

import java.lang.reflect.Member
import java.lang.reflect.Method

data class AfterParams(
    private val mMember: () -> Member,
    private val mArgs: () -> Array<Any?>,
    private val mInstance: () -> Any?,
    private val mResult: () -> Any?,
    private val mThrowable: () -> Throwable?,
    private val mIsSkipped: () -> Boolean,
    private val mSetResult: (Any?) -> Unit,
    private val mSetThrowable: (Throwable?) -> Unit,
) {
    val member get() = mMember.invoke()

    val method get() = mMember.invoke() as Method

    val args get() = mArgs.invoke()

    val instance get() = mInstance.invoke()

    inline fun <reified T> instanceOf(): T = instance as T

    inline fun <reified T> instanceOfOrNull(): T? = instance as? T

    var result: Any?
        get() = mResult.invoke()
        set(value) = mSetResult.invoke(value)

    inline fun <reified T> resultOf(): T = result as T

    inline fun <reified T> resultOfNull(): T? = result as? T

    var throwable: Throwable?
        get() = mThrowable.invoke()
        set(value) = mSetThrowable.invoke(value)

    val isSkipped get() = mIsSkipped.invoke()

    override fun toString(): String {
        // format json
        // example: {"member": "method", "args": [{"index": 0, "type": "class java.lang.String", "value": "hello"}], "instance": {"type": "class java.lang.String", "value": "hello"}, "result": {"type": "class java.lang.String", "value": "hello"}, "throwable": {"type": "class java.lang.Exception", "value": "null"}, "isSkipped": "false"}
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
        builder.append(", ")

        builder.append("\"result\": {")
        builder.append("\"type\": \"${result?.javaClass}\"")
        builder.append(", ")
        builder.append("\"value\": \"${result?.toString()?.replace("\"", "\\\"")}\"")
        builder.append("}")
        builder.append(", ")

        builder.append("\"throwable\": {")
        builder.append("\"type\": \"${throwable?.javaClass}\"")
        builder.append(", ")
        builder.append("\"value\": \"${throwable?.toString()?.replace("\"", "\\\"")}\"")
        builder.append("}")
        builder.append(", ")

        builder.append("\"isSkipped\": \"${isSkipped}\"")
        builder.append("}")
        return builder.toString()
    }
}