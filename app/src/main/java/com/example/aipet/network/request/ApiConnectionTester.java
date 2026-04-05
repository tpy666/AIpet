package com.example.aipet.network.request;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aipet.network.ApiConfig;
import com.example.aipet.network.ChatRequest;
import com.example.aipet.network.ChatResponse;
import com.example.aipet.network.DoubaoRequest;
import com.example.aipet.network.OpenAIChatRequest;
import com.example.aipet.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.service.ArkService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * API 连接测试器。
 *
 * 统一封装设置页的连接检测逻辑，避免在 UI 层手写分散的请求拼装和成功判定。
 */
public final class ApiConnectionTester {

    private static final String TAG = "ApiConnectionTester";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final Gson GSON = new GsonBuilder().create();

    private ApiConnectionTester() {
    }

    public interface Callback {
        void onSuccess(@NonNull String message);

        void onFailure(@NonNull String message);
    }

    public static void testConnection(@NonNull String provider,
                                      @NonNull String apiUrl,
                                      @NonNull String apiKey,
                                      @NonNull String modelName,
                                      @NonNull Callback callback) {
        new Thread(() -> {
            try {
                ApiConfig.ApiProvider apiProvider = resolveProvider(provider);
                if (apiProvider == ApiConfig.ApiProvider.DOUBAO) {
                    testDoubaoViaSdk(apiUrl, apiKey, modelName, callback);
                    return;
                }

                String normalizedUrl = normalizeBaseUrl(apiProvider, apiUrl);
                if (normalizedUrl.isEmpty() && apiProvider == ApiConfig.ApiProvider.CUSTOM) {
                    callback.onFailure("请先填入 API URL");
                    return;
                }

                String finalUrl = buildRequestUrl(apiProvider, normalizedUrl);
                String requestBodyJson = buildRequestBody(apiProvider, modelName);

                Log.d(TAG, "测试请求 URL: " + finalUrl);
                Log.d(TAG, "测试请求体: " + requestBodyJson);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .build();

                Request.Builder requestBuilder = new Request.Builder()
                        .url(finalUrl)
                        .post(RequestBody.create(requestBodyJson, JSON_TYPE))
                        .addHeader("Content-Type", "application/json");

                if (!apiKey.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
                }

                Request request = requestBuilder.build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        callback.onFailure(buildHttpFailureMessage(response.code(), readBody(response)));
                        return;
                    }

                    String responseBody = readBody(response);
                    String successMessage = validateResponse(apiProvider, responseBody, response.code());
                    if (successMessage != null) {
                        callback.onSuccess(successMessage);
                    } else {
                        callback.onFailure(buildEmptyResponseMessage(apiProvider, response.code()));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "连接测试异常", e);
                callback.onFailure("连接错误: " + safeMessage(e));
            }
        }, "api-connection-test").start();
    }

    private static void testDoubaoViaSdk(@NonNull String apiUrl,
                                         @NonNull String apiKey,
                                         @NonNull String modelName,
                                         @NonNull Callback callback) {
        if (apiKey.trim().isEmpty()) {
            callback.onFailure("豆包 API Key 不能为空");
            return;
        }

        String normalizedBaseUrl = normalizeDoubaoBaseUrl(apiUrl);
        if (normalizedBaseUrl.isEmpty()) {
            callback.onFailure("豆包 API URL 不能为空");
            return;
        }

        ArkService service = null;
        try {
            service = ArkService.builder()
                    .apiKey(apiKey.trim())
                    .baseUrl(normalizedBaseUrl)
                    .build();

            CreateResponsesRequest request = CreateResponsesRequest.builder()
                    .model(modelName.trim())
                    .input(ResponsesInput.builder()
                            .stringValue("connectivity_check")
                            .build())
                    .build();

            ResponseObject response = service.createResponse(request);
            if (response == null || response.getOutput() == null) {
                callback.onFailure("连接失败: 豆包返回空响应");
                return;
            }

            String output = response.getOutput().toString();
            if (output == null || output.trim().isEmpty()) {
                callback.onFailure("连接失败: 豆包返回无有效回复");
                return;
            }

            callback.onSuccess("✓ 连接成功 (Doubao SDK)");
        } catch (Exception e) {
            callback.onFailure("连接失败: " + safeMessage(e));
        } finally {
            if (service != null) {
                try {
                    service.shutdownExecutor();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private static ApiConfig.ApiProvider resolveProvider(@NonNull String provider) {
        if (Constants.PROVIDER_OPENAI.equalsIgnoreCase(provider)) {
            return ApiConfig.ApiProvider.OPENAI;
        }
        if (Constants.PROVIDER_DOUBAO.equalsIgnoreCase(provider)) {
            return ApiConfig.ApiProvider.DOUBAO;
        }
        if (Constants.PROVIDER_LOCAL.equalsIgnoreCase(provider)) {
            return ApiConfig.ApiProvider.LOCAL_BACKEND;
        }
        return ApiConfig.ApiProvider.CUSTOM;
    }

    private static String normalizeBaseUrl(@NonNull ApiConfig.ApiProvider provider, @NonNull String apiUrl) {
        String trimmedUrl = apiUrl.trim();
        if (trimmedUrl.isEmpty()) {
            return trimmedUrl;
        }
        if (provider == ApiConfig.ApiProvider.OPENAI || provider == ApiConfig.ApiProvider.DOUBAO) {
            return trimmedUrl;
        }
        if (trimmedUrl.endsWith("/")) {
            return trimmedUrl.substring(0, trimmedUrl.length() - 1);
        }
        return trimmedUrl;
    }

    private static String normalizeDoubaoBaseUrl(@NonNull String apiUrl) {
        String trimmed = apiUrl.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        if (trimmed.endsWith("/responses")) {
            return trimmed.substring(0, trimmed.length() - "/responses".length());
        }
        if (trimmed.endsWith("responses")) {
            return trimmed.substring(0, trimmed.length() - "responses".length());
        }
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String buildRequestUrl(@NonNull ApiConfig.ApiProvider provider, @NonNull String baseUrl) {
        if (provider == ApiConfig.ApiProvider.OPENAI) {
            return ensureTrailingSlash(baseUrl) + "v1/chat/completions";
        }
        if (provider == ApiConfig.ApiProvider.DOUBAO) {
            return ensureTrailingSlash(baseUrl) + "responses";
        }
        if (provider == ApiConfig.ApiProvider.LOCAL_BACKEND) {
            return ensureTrailingSlash(baseUrl) + "api/chat";
        }
        return baseUrl;
    }

    private static String buildRequestBody(@NonNull ApiConfig.ApiProvider provider, @NonNull String modelName) {
        if (provider == ApiConfig.ApiProvider.DOUBAO) {
            return GSON.toJson(new DoubaoRequest(modelName, "connectivity_check"));
        }
        if (provider == ApiConfig.ApiProvider.OPENAI) {
            return GSON.toJson(new OpenAIChatRequest(modelName, null, "connectivity_check"));
        }
        return GSON.toJson(new ChatRequest("connectivity_check", 0L));
    }

    @Nullable
    private static String validateResponse(@NonNull ApiConfig.ApiProvider provider,
                                           @NonNull String responseBody,
                                           int statusCode) {
        if (responseBody.trim().isEmpty()) {
            return null;
        }

        if (provider == ApiConfig.ApiProvider.DOUBAO) {
            String reply = extractDoubaoReply(responseBody);
            return reply != null && !reply.trim().isEmpty()
                    ? "✓ 连接成功 (HTTP " + statusCode + ")"
                    : null;
        }

        ChatResponse chatResponse = GSON.fromJson(responseBody, ChatResponse.class);
        if (chatResponse != null) {
            String reply = chatResponse.getReplyContent();
            if (reply != null && !reply.trim().isEmpty()) {
                return "✓ 连接成功 (HTTP " + statusCode + ")";
            }
        }
        return null;
    }

    @Nullable
    private static String extractDoubaoReply(@NonNull String responseBody) {
        try {
            JsonElement root = JsonParser.parseString(responseBody);
            return extractDoubaoReplyFromElement(root);
        } catch (Exception e) {
            Log.w(TAG, "解析豆包响应失败: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    private static String extractDoubaoReplyFromElement(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonPrimitive()) {
            String value = element.getAsString();
            return value != null && !value.trim().isEmpty() ? value : null;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                String reply = extractDoubaoReplyFromElement(item);
                if (reply != null && !reply.trim().isEmpty()) {
                    return reply;
                }
            }
            return null;
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has("error") && !object.get("error").isJsonNull()) {
            return null;
        }

        if (object.has("output")) {
            String reply = extractDoubaoReplyFromElement(object.get("output"));
            if (reply != null && !reply.trim().isEmpty()) {
                return reply;
            }
        }

        if (object.has("choices")) {
            String reply = extractChoiceReply(object.get("choices"));
            if (reply != null && !reply.trim().isEmpty()) {
                return reply;
            }
        }

        if (object.has("message")) {
            String reply = extractMessageContent(object.get("message"));
            if (reply != null && !reply.trim().isEmpty()) {
                return reply;
            }
        }

        if (object.has("content") && object.get("content").isJsonPrimitive()) {
            String content = object.get("content").getAsString();
            if (content != null && !content.trim().isEmpty()) {
                return content;
            }
        }

        if (object.has("text") && object.get("text").isJsonPrimitive()) {
            String text = object.get("text").getAsString();
            if (text != null && !text.trim().isEmpty()) {
                return text;
            }
        }

        if (object.has("reply") && object.get("reply").isJsonPrimitive()) {
            String reply = object.get("reply").getAsString();
            if (reply != null && !reply.trim().isEmpty()) {
                return reply;
            }
        }

        for (String key : new String[]{"result", "answer", "output_text"}) {
            if (object.has(key) && object.get(key).isJsonPrimitive()) {
                String value = object.get(key).getAsString();
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            }
        }

        return null;
    }

    @Nullable
    private static String extractChoiceReply(@Nullable JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return null;
        }
        for (JsonElement item : element.getAsJsonArray()) {
            if (item == null || item.isJsonNull() || !item.isJsonObject()) {
                continue;
            }
            JsonObject choice = item.getAsJsonObject();
            if (choice.has("message")) {
                String reply = extractMessageContent(choice.get("message"));
                if (reply != null && !reply.trim().isEmpty()) {
                    return reply;
                }
            }
            if (choice.has("text") && choice.get("text").isJsonPrimitive()) {
                String text = choice.get("text").getAsString();
                if (text != null && !text.trim().isEmpty()) {
                    return text;
                }
            }
            if (choice.has("output_text") && choice.get("output_text").isJsonPrimitive()) {
                String outputText = choice.get("output_text").getAsString();
                if (outputText != null && !outputText.trim().isEmpty()) {
                    return outputText;
                }
            }
        }
        return null;
    }

    @Nullable
    private static String extractMessageContent(@Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            String value = element.getAsString();
            return value != null && !value.trim().isEmpty() ? value : null;
        }
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject message = element.getAsJsonObject();
        if (message.has("content")) {
            JsonElement content = message.get("content");
            if (content.isJsonPrimitive()) {
                String value = content.getAsString();
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            } else if (content.isJsonArray()) {
                StringBuilder builder = new StringBuilder();
                for (JsonElement part : content.getAsJsonArray()) {
                    String reply = extractDoubaoReplyFromElement(part);
                    if (reply != null && !reply.trim().isEmpty()) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(reply);
                    }
                }
                if (builder.length() > 0) {
                    return builder.toString();
                }
            }
        }
        if (message.has("text") && message.get("text").isJsonPrimitive()) {
            String text = message.get("text").getAsString();
            if (text != null && !text.trim().isEmpty()) {
                return text;
            }
        }
        if (message.has("output_text") && message.get("output_text").isJsonPrimitive()) {
            String outputText = message.get("output_text").getAsString();
            if (outputText != null && !outputText.trim().isEmpty()) {
                return outputText;
            }
        }
        return null;
    }

    private static String buildHttpFailureMessage(int code, @Nullable String body) {
        String trimmedBody = body == null ? "" : body.trim();
        if (trimmedBody.isEmpty()) {
            return "连接失败: HTTP " + code;
        }
        return "连接失败: HTTP " + code + " (" + abbreviate(trimmedBody, 120) + ")";
    }

    private static String buildEmptyResponseMessage(@NonNull ApiConfig.ApiProvider provider, int code) {
        return provider == ApiConfig.ApiProvider.DOUBAO
                ? "连接失败: HTTP " + code + "，但返回的豆包响应体无有效回复"
                : "连接失败: HTTP " + code + "，但返回的响应体无有效回复";
    }

    private static String readBody(@NonNull Response response) throws IOException {
        if (response.body() == null) {
            return "";
        }
        return response.body().string();
    }

    private static String ensureTrailingSlash(@NonNull String url) {
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }

    private static String abbreviate(@NonNull String value, int maxLength) {
        return value.substring(0, Math.min(maxLength, value.length()));
    }

    private static String safeMessage(@Nullable Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().trim().isEmpty()) {
            return "未知网络错误";
        }
        return throwable.getMessage();
    }
}