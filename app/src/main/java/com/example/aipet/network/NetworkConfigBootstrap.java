package com.example.aipet.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.aipet.util.Constants;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.ApiSettingsStore;

/**
 * Network 配置总引导。
 *
 * 负责把持久化设置同步回运行时 ApiConfig，避免应用重启后回落到默认 provider。
 */
public final class NetworkConfigBootstrap {

    private static final String TAG = "NetworkConfigBootstrap";

    private NetworkConfigBootstrap() {
    }

    public static void syncFromStorage(@NonNull Context context) {
        ApiSettingsStore.ApiSettings settings = UtilHub.apiSettingsStore(context).load();

        String provider = safe(settings.provider);
        String apiUrl = safe(settings.apiUrl);
        String apiKey = safe(settings.apiKey);
        String modelName = safe(settings.modelName);

        try {
            if (Constants.PROVIDER_DOUBAO.equals(provider)) {
                ApiClient.configureDoubao(
                        resolveOrDefault(apiUrl, Constants.DOUBAO_BASE_URL),
                        apiKey,
                        resolveOrDefault(modelName, Constants.DOUBAO_DEFAULT_MODEL)
                );
                Log.i(TAG, "已同步豆包配置");
                return;
            }

            if (Constants.PROVIDER_OPENAI.equals(provider)) {
                ApiClient.configureOpenAI(
                        apiKey,
                        resolveOrDefault(modelName, Constants.OPENAI_DEFAULT_MODEL)
                );
                Log.i(TAG, "已同步 OpenAI 配置");
                return;
            }

            if (Constants.PROVIDER_LOCAL.equals(provider)) {
                ApiClient.configureLocalBackend(resolveOrDefault(apiUrl, Constants.LOCAL_BACKEND_URL));
                Log.i(TAG, "已同步本地后端配置");
                return;
            }

            if (Constants.PROVIDER_CUSTOM.equals(provider)) {
                String customUrl = resolveOrDefault(apiUrl, Constants.CUSTOM_API_EXAMPLE_URL);
                String customModel = resolveOrDefault(modelName, Constants.OPENAI_DEFAULT_MODEL);
                ApiConfig.getInstance().configureCustom(customUrl, apiKey, customModel);
                ApiClient.reinitializeApiService();
                Log.i(TAG, "已同步自定义配置");
                return;
            }

            Log.w(TAG, "未识别 provider，保持当前配置: " + provider);
        } catch (Exception e) {
            Log.e(TAG, "同步网络配置失败: " + e.getMessage(), e);
        }
    }

    @NonNull
    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @NonNull
    private static String resolveOrDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
