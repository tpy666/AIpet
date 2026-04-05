package com.example.aipet.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.aipet.util.store.ApiSettingsStore;
import com.example.aipet.util.store.PetStore;

/**
 * Util 总引导入口
 *
 * 集中暴露 util 能力访问点，
 * 具体能力由分布式子模块实现。
 */
public final class UtilHub {

    private UtilHub() {
    }

    public static ChatLogger logger(@NonNull Context context) {
        return ChatLogger.getInstance(context);
    }

    public static PetStore petStore(@NonNull Context context) {
        return new PetStore(context);
    }

    public static ApiSettingsStore apiSettingsStore(@NonNull Context context) {
        return new ApiSettingsStore(context);
    }
}
