package io.github.xpler_example.module;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import io.github.xpler2.XplerModuleInterface;
import io.github.xpler2.callback.HookerCallback;
import io.github.xpler2.params.AfterParams;
import io.github.xpler2.params.BeforeParams;

public class InitJava {
    /* @XplerInitialize(
            name = "$random$",
            description = "This is a Java module for Xpler.",
            scope = {"com.example.app"},
            xposed = false, // Turn off xposed support
            lsposed = true
    ) */
    public static void init(XplerModuleInterface module) {
        try {
            if (!module.isFirstPackage()) return; // skip if not the first package
            if (!module.getPackageName().equals("com.example.app")) return;// skip if not the target package
            if (module.getProcessName().contains(":")) return; // skip sub-processes

            Log.i("Xpler2", "[Xpler2]Java -> init called in `" + module.getPackageName() + "` process: " + module.getProcessName());

            Class<?> tinkerApplicationClazz = module.getClassLoader().loadClass("com.example.app.MyApplication");
            Method onCreateMethod = tinkerApplicationClazz.getDeclaredMethod("onCreate");
            module.hooker(onCreateMethod, new HookerCallback() {
                @Override
                public void onBefore(@NonNull BeforeParams params) {
                    getModule().log("[Xpler2]Java -> onCreate called in MyApplication: " + params);
                }

                @Override
                public void onAfter(@NonNull AfterParams params) {
                    getModule().log("[Xpler2]Java -> MyApplication onCreate finished.");
                }
            });
        } catch (Throwable e) {
            module.log("[Xpler2]Java -> InitJava not init.", e);
        }
    }
}
