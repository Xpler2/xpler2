# Xpler2

![maven-central](https://img.shields.io/maven-central/v/io.github.xpler2/xpler2)

Xposed/Lsposed Kotlin 开发包, 更适合Kotlin的编码风格。

Xpler2 在原 Xposed/Lsposed Api 基础上进行封装, 使其支持Kotlin的DSL特性, 更简洁的编写Hook逻辑。

## 食用方法

```kotlin
// 在app模块的 build.gradle.kts 中添加以下内容
plugins {
    ...
    id("io.github.xpler2.compiler") version "<last-version>"
}

dependencies {
    ...
    implementation("io.github.xpler2:xpler2:<last-version>")
}
```

## 模块入口

在项目中写入以下代码

```kotlin
// file: com.example.app.Init.kt

@XplerInitialize(
    name = "com.example.ModuleInit",
    description = "This is a Kotlin module for Xpler.",
    scope = ["com.example.app"],
    xposed = false, // Turn off xposed support
    lsposed = true
)
fun init(module: XplerModuleInterface) { // must be `public static` and parameter must be single XplerModuleInterface
    module.log("Kotlin module initialized")
}
```

或者, 如果你需要不固定的入口类, 可以使用占位字符串 `$random$` 来代替, compiler plugin 将会自动将它随机.

```kotlin
// file: com.example.app.Init.kt

@XplerInitialize(
    name = $$"$random$",
    description = "This is a Kotlin module for Xpler.",
    scope = ["com.example.app"],
    xposed = false, // Turn off xposed support
    lsposed = true
)
fun init(module: XplerModuleInterface) { // must be `public static` and parameter must be single XplerModuleInterface
    module.log("Kotlin module initialized")
}
```

> note: 入口方法名可随意定义, 但参数只允许有一个`XplerModuleInterface`, 并且访问权限必须是
`public static`

## 模块状态

```kotlin
class ModuleMainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        val instance = XplerModuleStatus.instance
        statusTv.text = "isActivate: ${instance?.isActivate}"
        ...
    }
}
```

## Hook逻辑

```kotlin
@XplerInitialize(...)
fun domain(module: XplerModuleInterface) {
    val method = YouCustomFindUtil.findMethod(...)

    // dsl
    module.hooker(method) {
        onBefore { // this scope is for BeforeParams
            val first = this.args[0]
            ...
        }
        onAfter { // this scope is for AfterParams
            val isSkipped = this.isSkipped()
            ...
        }
    }

    // anonymous inner class
    module.hooker(method, object : HookerCallback() {
        override fun onBefore(params: BeforeParams) {
            val first = params.args[0]
            ...
        }
        override fun onAfter(params: AfterParams) {
            val isSkipped = params.isSkipped()
            ...
        }
    })
}
```

## 运行

最后, 点击右上角的Run按钮, 即可完成一个简单的Xposed模块, compiler plugin会自行解析
`@XplerInitialize` 并完成模块入口`xposed/lsposed`的配置, 无需其它额外的操作.

关于Xpler2文档及使用案例, 请查看本项目的`app`模块和相关方法注释

## 其它说明

- 若打算运行本项目,
  你需要首先参考 [LSPosed-Wiki](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API#early-access)
  拉取 [api](https://github.com/libxposed/api) 进行 `publishToMavenLocal`.

- 注意, Xpler2使用kotlin编写, 不含kotlin相关依赖的纯java项目, 可能需要自行添加kotlin标准库
  `kotlin-stdlib`的依赖支持。

- 另外，在release混淆时, 请注意保留你的混淆入口, 如:

  ```kotlin
  -keep, allowobfuscation class com.example.app.Init { 
      public static **(io.github.xpler2.XplerModuleInterface);
  }
  ```

## 致谢

- [EzXHelper](https://github.com/KyuubiRan/EzXHelper) - onBefore 和 onAfter 分发机制来源于该项目
