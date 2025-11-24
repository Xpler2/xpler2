package io.github.xpler2

/**
 * Annotation to mark a class as an Xpler module initializer.
 * This annotation is used to provide metadata about the module,
 * such as its name, description, scope, and support for Xposed and Lsposed frameworks.
 *
 * ```
 * // ---------------- java example:
 * package com.example;
 *
 * public class InitJava {
 *     @XplerInitialize(
 *           name = "com.example.ModuleInit",
 *           description = "This is a Java module for Xpler.",
 *           scope = {"com.example.app"},
 *           xposed = false, // Turn off xposed support
 *           lsposed = true
 *      )
 *      public static void init(XplerModuleInterface module) { // must be `public static` and parameter must be single XplerModuleInterface
 *          module.log("Java module initialized");
 *      }
 * }
 *
 * // ---------------- kotlin example1:
 * package com.example
 *
 * @XplerInitialize(
 *      name = "com.example.ModuleInit",
 *      description = "This is a Kotlin module for Xpler.",
 *      scope = ["com.example.app"],
 *      xposed = false, // Turn off xposed support
 *      lsposed = true
 * )
 * fun init(module: XplerModuleInterface) { // must be `public static` and parameter must be single XplerModuleInterface
 *    module.log("Kotlin module initialized")
 * }
 *
 * // ---------------- kotlin example2:
 * package com.example
 *
 * class InitKotlin {
 *      companion object {
 *        @XplerInitialize(
 *           name = "com.example.ModuleInit",
 *           description = "This is a Kotlin module for Xpler.",
 *           scope = ["com.example.app"],
 *           xposed = false, // Turn off xposed support
 *           lsposed = true
 *         )
 *         @JvmStatic // must be static for Java compatibility
 *         fun init(module: XplerModuleInterface) { // must be `public static` and parameter must be single XplerModuleInterface
 *             module.log("Kotlin module initialized")
 *         }
 *     }
 * }
 *
 * ```
 *
 * @property xposed Configuration for Xposed framework support.
 * @property lsposed Configuration for Lsposed framework support.
 * @property name The name of the module class, example: `com.example.ModuleInit` or `$random$`.
 * @property description A brief description of the module, example: `This module does XYZ`.
 * @property scope An array of app package names that the module should be applied to.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class XplerInitialize(
    /**
     * Initialize the module class name, If you want a random entrance, you can try using `$random$`
     */
    val name: String,

    /**
     * module description
     */
    val description: String,

    /**
     * module scope app package name
     */
    val scope: Array<String>,

    /**
     * Enable Xposed framework support
     */
    val xposed: Boolean = true,

    /**
     * The version of the Xposed API that this module is compatible with.
     * Default is 82, which corresponds to Xposed API version 82.
     */
    val xposedVersion: Int = 82,

    /**
     * Enable Lsposed framework support
     */
    val lsposed: Boolean = true,

    /**
     * Indicates the target Xposed API version required by the module
     * Default is 100, which corresponds to Lsposed API version 100.
     */
    val lsposedTargetVersion: Int = 100,

    /**
     * Indicates the minimal Xposed API version required by the module
     * Default is 100, which corresponds to Lsposed API version 100.
     */
    val lsposedMinVersion: Int = 100,

    /**
     * Indicates whether users should not apply the module on any other app out of scope.
     * Default is true, which means the module is static scope.
     */
    val lsposedStaticScope: Boolean = true,

    /**
     * Does it support the old version annotation API
     * ```
     *  @io.github.libxposed.api.annotations.AfterInvocation
     *  @io.github.libxposed.api.annotations.BeforeInvocation
     *  @io.github.libxposed.api.annotations.XposedHooker
     * ```
     */
    val lsposedCompatAnnotation: Boolean = true
)