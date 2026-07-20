# Xposed
-keepclassmembers class ** implements de.robv.android.xposed.IXposedHookLoadPackage {
    public <init>();
    public void initZygote(...);
    public void handleLoadPackage(...);
}

-keep class io.github.xpler2.xposed.XposedStatus {
    *;
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

## 以下是备注说明，避免忘记
# -keepclassmembers 允许混淆类名，但是允许保留成员名不被混淆(简单来说就是被保留的类成员名不被混淆，类名可以被混淆)
# -keep,allowobfuscation 保留类成员，但是允许混淆类名和成员名(简单来说就是被保留的都可以被混淆)