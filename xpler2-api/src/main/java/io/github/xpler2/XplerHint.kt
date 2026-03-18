package io.github.xpler2

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class XplerHint(
    /**
     * Initialize the module class name. If you want a random entry, simply pass in a blank string
     */
    val name: String = "",

    /**
     * module description
     */
    val description: String,

    /**
     * module scope app package name
     */
    val scope: Array<String>,
)
