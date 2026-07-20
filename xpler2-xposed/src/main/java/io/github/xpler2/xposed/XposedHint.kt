package io.github.xpler2.xposed

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class XposedHint(
    val version: Int = 82,
)