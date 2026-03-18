# Xpler2
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

## 以下是备注说明，避免忘记
# -keepclassmembers 允许混淆类名，但是允许保留成员名不被混淆(简单来说就是被保留的类成员名不被混淆，类名可以被混淆)
# -keep,allowobfuscation 保留类成员，但是允许混淆类名和成员名(简单来说就是被保留的都可以被混淆)