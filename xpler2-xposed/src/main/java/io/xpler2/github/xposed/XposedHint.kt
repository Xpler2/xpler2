package io.xpler2.github.xposed

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class XposedHint(
    val version: Int = 82,
)