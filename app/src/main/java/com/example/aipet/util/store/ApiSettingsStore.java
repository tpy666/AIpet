package com.example.aipet.util.store;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.aipet.util.Constants;
import com.example.aipet.util.SPUtils;

/**
 * API 设置存储模块
 */
public class ApiSettingsStore {

    private final Context appContext;

    public ApiSettingsStore(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    public ApiSettings load() {
        String provider = SPUtils.getString(appContext, Constants.KEY_API_PROVIDER, Constants.PROVIDER_OPENAI);
        String apiUrl = SPUtils.getString(appContext, Constants.KEY_API_URL, "");
        String apiKey = SPUtils.getString(appContext, Constants.KEY_API_KEY, "");
        String modelName = SPUtils.getString(appContext, Constants.KEY_MODEL_NAME, Constants.OPENAI_DEFAULT_MODEL);
        String avatarImageUrl = SPUtils.getString(appContext, Constants.KEY_AVATAR_IMAGE_URL, "");
        String avatarUploadUrl = SPUtils.getString(appContext, Constants.KEY_AVATAR_UPLOAD_URL, "");
        String avatarRemoveBgUrl = SPUtils.getString(appContext, Constants.KEY_AVATAR_REMOVE_BG_URL, "");
        boolean avatarAutoProcess = SPUtils.getString(appContext, Constants.KEY_AVATAR_AUTO_PROCESS, "false").equals("true");
        boolean avatarPreserveOriginal = SPUtils.getString(appContext, Constants.KEY_AVATAR_PRESERVE_ORIGINAL, "false").equals("true");
        return new ApiSettings(provider, apiUrl, apiKey, modelName, avatarImageUrl, avatarUploadUrl, avatarRemoveBgUrl, avatarAutoProcess, avatarPreserveOriginal);
    }

    public void save(@NonNull ApiSettings settings) {
        SPUtils.putString(appContext, Constants.KEY_API_PROVIDER, settings.provider);
        SPUtils.putString(appContext, Constants.KEY_API_URL, settings.apiUrl);
        SPUtils.putString(appContext, Constants.KEY_API_KEY, settings.apiKey);
        SPUtils.putString(appContext, Constants.KEY_MODEL_NAME, settings.modelName);
        SPUtils.putString(appContext, Constants.KEY_AVATAR_IMAGE_URL, settings.avatarImageUrl);
        SPUtils.putString(appContext, Constants.KEY_AVATAR_UPLOAD_URL, settings.avatarUploadUrl);
        SPUtils.putString(appContext, Constants.KEY_AVATAR_REMOVE_BG_URL, settings.avatarRemoveBgUrl);
        SPUtils.putString(appContext, Constants.KEY_AVATAR_AUTO_PROCESS, String.valueOf(settings.avatarAutoProcess));
        SPUtils.putString(appContext, Constants.KEY_AVATAR_PRESERVE_ORIGINAL, String.valueOf(settings.avatarPreserveOriginal));
    }

    public static class ApiSettings {
        public final String provider;
        public final String apiUrl;
        public final String apiKey;
        public final String modelName;
        public final String avatarImageUrl;
        public final String avatarUploadUrl;
        public final String avatarRemoveBgUrl;
        public final boolean avatarAutoProcess;
        public final boolean avatarPreserveOriginal;

        public ApiSettings(String provider, String apiUrl, String apiKey, String modelName,
                           String avatarImageUrl, String avatarUploadUrl, String avatarRemoveBgUrl,
                           boolean avatarAutoProcess, boolean avatarPreserveOriginal) {
            this.provider = provider;
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.modelName = modelName;
            this.avatarImageUrl = avatarImageUrl;
            this.avatarUploadUrl = avatarUploadUrl;
            this.avatarRemoveBgUrl = avatarRemoveBgUrl;
            this.avatarAutoProcess = avatarAutoProcess;
            this.avatarPreserveOriginal = avatarPreserveOriginal;
        }
    }
}
