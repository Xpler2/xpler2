# Lsposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation,allowoptimization public class ** extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onPackageLoaded(...);
    public void onSystemServerLoaded(...);
}
-keep,allowoptimization,allowobfuscation @io.github.libxposed.api.annotations.* class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}
-keepclassmembers,allowoptimization class ** implements io.github.libxposed.api.XposedInterface$Hooker {
    public static *** before();
    public static *** before(io.github.libxposed.api.XposedInterface$BeforeHookCallback);
    public static void after();
    public static void after(io.github.libxposed.api.XposedInterface$AfterHookCallback);
    public static void after(io.github.libxposed.api.XposedInterface$AfterHookCallback, ***);
}

# Xposed
-keepclassmembers class ** implements de.robv.android.xposed.IXposedHookLoadPackage {
    public <init>();
    public void initZygote(...);
    public void handleLoadPackage(...);
}

# Xpler2
-keep class ** extends io.github.xpler2.base.XposedEntrance {
    public <init>();
    public void initZygote(...);
    public void handleLoadPackage(...);
}
-keep class ** extends io.github.xpler2.base.LsposedEntrance {
    public <init>(...);
    public void onPackageLoaded(...);
    public void onSystemServerLoaded(...);
}
-keep,allowobfuscation class io.github.xpler2.callback.HookerCallback { *; }
-keep,allowobfuscation class ** extends io.github.xpler2.callback.HookerCallback {
    getModule();
    getUnhook(...);
    onBefore(...);
    onAfter(...);
}
-keep,allowobfuscation interface io.github.xpler2.callback.HookerFunction { *; }
-keep,allowobfuscation class ** implements io.github.xpler2.callback.HookerFunction {
    getModule();
    getUnhook(...);
    onBefore(...);
    onAfter(...);
}
-keepclassmembers class io.github.xpler2.base.LsposedModule$Hooker {
    public static *** before(...);
    public static *** after(...);
}
-keepclassmembers class io.github.xpler2.base.XposedModule$Hooker {
    beforeHookedMethod(...);
    afterHookedMethod(...);
}
-keep,allowobfuscation class io.github.xpler2.params.BeforeParams {
    get*();
    instance*(...);
    returnAndSkip(...);
    throwAndSkip(...);
}
-keep,allowobfuscation class io.github.xpler2.params.AfterParams {
    get*();
    set*(...);
    is*(...);
    instance*(...);
    result*(...);
}
-keep,allowobfuscation class io.github.xpler2.params.UnhookParams {
    get*();
    unhook(...);
}
-keep,allowobfuscation interface io.github.xpler2.XplerModuleInterface { *; }
-keep,allowobfuscation class ** implements io.github.xpler2.XplerModuleInterface {
    hooker(...);
    get*();
    modulePackageInfo(...);
    injectResource(...);
    deoptimize(...);
    invokeOrigin(...);
    invokeSpecial(...);
    newInstanceOrigin(...);
    newInstanceSpecial(...);
    getRemotePreferences(...);
    listRemoteFiles(...);
    openRemoteFile(...);
    log(...);
    stackTraceString(...);
}
-keep,allowobfuscation class io.github.xpler2.ExtKt {
    get*();
    set*(...);
    hooker(...);
}
-keep class io.github.xpler2.XplerModuleStatus {
    *;
}
-keep class ** implements io.github.xpler2.XplerModuleStatus {
    get*(...);
}
-keep,allowobfuscation class io.github.xpler2.XplerLogger {
    logger(...);
}

## 忽略警告-引用项目
-dontwarn de.robv.android.xposed.IXposedHookLoadPackage
-dontwarn de.robv.android.xposed.IXposedHookZygoteInit$StartupParam
-dontwarn de.robv.android.xposed.IXposedHookZygoteInit
-dontwarn de.robv.android.xposed.XC_MethodHook$Unhook
-dontwarn de.robv.android.xposed.XC_MethodHook
-dontwarn de.robv.android.xposed.XC_MethodHook$MethodHookParam
-dontwarn de.robv.android.xposed.XposedBridge
-dontwarn de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam
-dontwarn io.github.libxposed.api.XposedInterface$Hooker
-dontwarn io.github.libxposed.api.XposedInterface$MethodUnhooker
-dontwarn io.github.libxposed.api.XposedInterface
-dontwarn io.github.libxposed.api.XposedModule
-dontwarn io.github.libxposed.api.XposedModuleInterface$ModuleLoadedParam
-dontwarn io.github.libxposed.api.XposedModuleInterface$PackageLoadedParam
-dontwarn io.github.libxposed.api.XposedInterface$AfterHookCallback
-dontwarn io.github.libxposed.api.XposedInterface$BeforeHookCallback
-dontwarn io.github.libxposed.api.annotations.AfterInvocation
-dontwarn io.github.libxposed.api.annotations.BeforeInvocation
-dontwarn io.github.libxposed.api.annotations.XposedHooker
-dontwarn io.github.libxposed.service.XposedService$OnScopeEventListener
-dontwarn io.github.libxposed.service.XposedService
-dontwarn io.github.libxposed.service.XposedServiceHelper$OnServiceListener
-dontwarn io.github.libxposed.service.XposedServiceHelper

## 以下是备注说明，避免忘记
# -keepclassmembers 允许混淆类名，但是允许保留成员名不被混淆(简单来说就是被保留的类成员名不被混淆，类名可以被混淆)
# -keep,allowobfuscation 保留类成员，但是允许混淆类名和成员名(简单来说就是被保留的都可以被混淆)