# Xpler2

![maven-central](https://img.shields.io/maven-central/v/io.github.xpler2/xpler2)

Xposed Kotlin 开发包, 更适合Kotlin的编码风格。

Xpler2 在原 Xposed Api 基础上进一步封装, 使其支持Kotlin的DSL特性, 更简洁的编写Hook逻辑。

## 使用方法

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

然后在项目中写入以下代码

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

模块状态

```kotlin
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        val instance = XplerModuleStatus.getInstance()
        statusTv.text = "isActivate: ${instance?.isActivate}"
        ...
    }
}
```

最后, 点击右上角的Run按钮, 即可完成一个简单的Xposed模块逻辑。

关于Xpler2文档及使用案例, 请查看本项目的`app`模块和相关方法注释

若打算运行本项目你需要首先参考 [LSPosed-Wiki](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API#early-access) 拉取 [api](https://github.com/libxposed/api) 进行 `publishToMavenLocal`。

## 其它说明

- 注意: Xpler2使用kotlin编写, 不含kotlin相关依赖的纯java项目, 可能需要自行添加kotlin标准库`kotlin-stdlib`的依赖支持。

- 另外，在release混淆时, 请注意保留你的混淆入口, 如:

  ```kotlin
  -keep, allowobfuscation class com.example.app.Init { 
      public static **(io.github.xpler2.XplerModuleInterface);
  }
  ```

## 致谢

- [EzXHelper](https://github.com/KyuubiRan/EzXHelper) - onBefore 和 onAfter 分发机制来源于该项目
