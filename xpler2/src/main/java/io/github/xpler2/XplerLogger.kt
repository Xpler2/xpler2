package io.github.xpler2

import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object XplerLogger {
    private val dateFormatHolder = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)
        }
    }

    // get the current time in the format yyyy-MM-dd'T'HH:mm:ss.SSS
    fun times(): String {
        return dateFormatHolder.get()!!.format(Date())
    }

    // prints the logs to the manager
    fun logger(message: String) {
        logger(message, null)
    }

    // prints the logs to the manager
    fun logger(message: String, throwable: Throwable?) {
        xplerModule.log(message, throwable)
    }
}