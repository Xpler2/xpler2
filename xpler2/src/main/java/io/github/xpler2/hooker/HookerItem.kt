package io.github.xpler2.hooker

/**
 * IHookerEntity collect annotation
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class HookerItem(
    val descr: String = "",
)
