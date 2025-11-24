package io.github.xpler_example.module;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Member;

import io.github.xpler2.hooker.HookerItem;
import io.github.xpler2.params.AfterParams;
import io.github.xpler2.params.BeforeParams;

@HookerItem
public class JavaEntity extends BaseHookEntity {

    @Nullable
    @Override
    public Member singleTarget() {
        try {
            Class<?> cls = getModule().getClassLoader().loadClass("com.example.app.MainActivity");
            return cls.getMethod("onCreate", Bundle.class);
        } catch (Throwable t) {
            Log.d("Xpler2", "target error: " + t.getMessage(), t);
        }
        return null;
    }

    @Override
    public void onBefore(@NonNull BeforeParams params) {
        Log.d("Xpler2", "JavaEntity onBefore: " + params);
    }

    @Override
    public void onAfter(@NonNull AfterParams params) {
        Log.d("Xpler2", "JavaEntity onAfter: " + params);
    }
}
