# Xpler2

![maven-central](https://img.shields.io/maven-central/v/io.github.xpler2/xpler2-api)

Xposed Kotlin 开发包, 更适合Kotlin的编码风格。

Xpler2 在原 Xposed Api 基础上进一步封装, 使其支持Kotlin的DSL特性, 更简洁的编写Hook逻辑。

> 注意: 自 `0.0.20` 起将不支持纯Java项目, 并且xpler2不再支持Java入口

## 食用方法

```kotlin
// 在app模块的 build.gradle.kts 中添加以下内容
plugins {
    ...
    id("io.github.xpler2.compiler") version "<version>"
}

dependencies {
    ...
    implementation("io.github.xpler2:xpler2-api:<version>")
    implementation("io.github.xpler2:xpler2-xposed:<version>")
}
```

## 模块入口

当前版本使用 `@XplerHint` 和 `@XposedHint` 标记唯一入口函数：

```kotlin
@XposedHint(version = 82)
@XplerHint(
    description = "Example module",
    scope = ["com.example.target"],
)
fun init(module: XplerModuleInterface) {
    module.log("module loaded: ${module.packageName}")
}
```

入口函数约束如下：

- 必须是 Kotlin 顶层函数。
- 只能有一个参数，类型必须是 `XplerModuleInterface`。
- 有且只能有一个`@XplerHint`存在。

`@XposedHint` 是 Xposed 开关：

- 未添加 `@XposedHint` 时，不生成 Xposed 入口、资源、Manifest 或混淆规则。
- compiler 根据是否声明 `xpler2-xposed` 依赖提供 Xposed 编译 API，不在 Gradle Sync 阶段解析入口源码。

## Hook DSL

常见写法如下：

```kotlin
package com.example.module

import io.github.xpler2.XplerHint
import io.github.xpler2.XplerModuleInterface
import io.github.xpler2.hooker
import io.github.xpler2.xposed.XposedHint

@XposedHint
@XplerHint(
    description = "Example module",
    scope = ["com.example.target"],
)
fun init(module: XplerModuleInterface) {
    if (!module.isFirstPackage) return
    if (module.packageName != "com.example.target") return
    if (":" in module.processName) return

    val targetClass = module.classLoader.loadClass("com.example.target.TargetClass")
    val targetMethod = targetClass.getDeclaredMethod("targetMethod")

    targetMethod.hooker {
        onBefore {
            module.log("before: $this")
        }

        onAfter {
            module.log("after: $this")
        }
    }
}
```

## 模块状态

模块 App 中直接通过：

```kotlin
val status = XplerModuleStatus.instance

if (status.isActivate) {
    println("${status.frameworkName} / api=${status.apiVersion}")
}
```

## Xposed 专属能力

如果你需要访问 Xposed 侧扩展对象，可以使用：

```kotlin
import io.github.xpler2.xposed.asXposed

// asXposed
module.asXposed?.run { xposed ->
    xposed.log("hello from xposed")
}

// withXposed
module.withXposed {
    this.log("hello from xposed")
}
```

在 `HookerCallback` / `HookerFunction` 作用域内，也可以直接使用 `withXposed { ... }`。

## 生成产物

执行构建后，编译器会在 `build/xpler2/` 下生成中间产物，常见文件包括：

- `entry.json`
- `libs/api-82.jar`
- `<variant>/xposed/source/<random-entry>.kt`
- `<variant>/xposed/source/XplerStatusProvider.kt`
- `<variant>/xposed/proguard-rules.pro`
- `<variant>/xposed/assets/xposed_init`
- `<variant>/xposed/res/values/values.xml`

另外，参与最终 manifest merge 的生成 manifest 由 AGP 接管，实际可在以下位置看到：

- `build/generated/manifests/xpler2GenerateConfig<Variant>/AndroidManifest.xml`

以上这些文件都属于`xpler2-compiler`插件的生成产物，不需要手工维护。

## 混淆

`xpler2-api` 和 `xpler2-xposed` 自带一部分 consumer rules，但如果你的应用开启了混淆，仍建议显式保留入口文件对应的顶层函数，例如：

```pro
-keep,allowobfuscation class com.example.module.InitKt {
    public static **(io.github.xpler2.XplerModuleInterface);
}
```

本仓库示例可参考 `app/proguard-rules.pro`。

## 示例工程

当前仓库中的 [Init.kt](app/src/main/java/io/github/xpler/example/module/Init.kt)
和 [MainActivity.kt](app/src/main/java/io/github/xpler/example/ui/MainActivity.kt) 就是最新可运行示例：

- `Init.kt` 展示入口注解和 Hook DSL。
- `MainActivity.kt` 展示 `XplerModuleStatus.instance` 的读取方式。

## Lsposed

关于Lsposed api-101, 需要等待至少有一个稳定的仓库发布后届时才再次加入

## 致谢

- [EzXHelper](https://github.com/KyuubiRan/EzXHelper) - onBefore 和 onAfter 分发机制来源于该项目
