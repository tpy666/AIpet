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
        return new ApiSettings(provider, apiUrl, apiKey, modelName);
    }

    public void save(@NonNull ApiSettings settings) {
        SPUtils.putString(appContext, Constants.KEY_API_PROVIDER, settings.provider);
        SPUtils.putString(appContext, Constants.KEY_API_URL, settings.apiUrl);
        SPUtils.putString(appContext, Constants.KEY_API_KEY, settings.apiKey);
        SPUtils.putString(appContext, Constants.KEY_MODEL_NAME, settings.modelName);
    }

    public static class ApiSettings {
        public final String provider;
        public final String apiUrl;
        public final String apiKey;
        public final String modelName;

        public ApiSettings(String provider, String apiUrl, String apiKey, String modelName) {
            this.provider = provider;
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.modelName = modelName;
        }
    }
}
