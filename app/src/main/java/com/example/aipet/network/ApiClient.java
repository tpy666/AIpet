package com.example.aipet.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * API 客户端 - Retrofit + OkHttp 集成（第二阶段升级版）
 * 
 * 功能：
 *   • 支持多个 API 端点动态切换
 *   • 自动重试机制（指数退避）
 *   • 请求超时控制
 *   • 详细的网络日志
 *   • 增强的错误处理
 *   • 宠物上下文信息支持
 */
@SuppressWarnings("unused")
public class ApiClient {

    private static final String TAG = "ApiClient";
    private static volatile ApiService apiService;
    private static volatile Retrofit retrofit;
    private static final Object serviceLock = new Object();
    private static final ApiConfig apiConfig = ApiConfig.getInstance();

    // ========== 回调接口 ==========
    
    /**
     * 聊天请求回调
     */
    public interface ChatCallback {
        /**
         * 请求成功
         * @param reply AI 回复内容
         */
        void onSuccess(String reply);

        /**
         * 请求失败
         * @param errorMessage 错误信息
         */
        void onFailure(String errorMessage);
    }

    /**
     * 完整响应回调（可获取 token 使用情况等）
     */
    public interface ChatResponseCallback {
        void onSuccess(ChatResponse response);
        void onFailure(String errorMessage);
    }

    // ========== API Service 接口 ==========
    
    /**
     * Retrofit API 服务定义
     */
    public interface ApiService {
        
        /**
         * 发送聊天消息 (POST /api/chat)
         * 用于本地后端或自定义 API
         */
        @POST("api/chat")
        Call<ChatResponse> sendMessage(@Body ChatRequest request);
        
        /**
         * 发送聊天消息到 OpenAI API (POST /v1/chat/completions)
         * 使用标准 OpenAI 格式
         */
        @POST("v1/chat/completions")
        Call<ChatResponse> sendOpenAIMessage(@Body OpenAIChatRequest request);
        
    }

    // ========== 单例获取 ==========
    
    /**
     * 获取 API 服务实例
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (serviceLock) {
                if (apiService == null) {
                    retrofit = buildRetrofit();
                    apiService = retrofit.create(ApiService.class);
                    
                    logDebug("API 服务已初始化 - 端点: " + apiConfig.getApiUrl());
                }
            }
        }
        return apiService;
    }

    /**
     * 重新初始化 API 服务（切换端点时使用）
     */
    public static void reinitializeApiService() {
        synchronized (serviceLock) {
            apiService = null;
            retrofit = null;
        }
        // 触发下一次的懒加载初始化
        getApiService();
        logDebug("API 服务已重新初始化");
    }

    // ========== 构建 Retrofit ==========
    
    /**
     * 构建 Retrofit 实例
     */
    private static Retrofit buildRetrofit() {
        OkHttpClient client = buildOkHttpClient();

        String baseUrl = resolveBaseUrl();
        
        // 确保 URL 以 / 结尾
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        
        // 创建自定义 Gson 实例，注册自定义反序列化器
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(DoubaoResponse.Output.class, new DoubaoResponse.OutputDeserializer())
                .create();
        
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private static String resolveBaseUrl() {
        ApiConfig.ApiProvider provider = apiConfig.getProvider();
        if (provider == ApiConfig.ApiProvider.LOCAL_BACKEND) {
            return ApiConfig.ApiProvider.LOCAL_BACKEND.baseUrl;
        }
        return apiConfig.getApiUrl();
    }

    /**
     * 构建 OkHttpClient 实例
     */
    private static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        
        // 1. 添加超时配置
        int timeout = apiConfig.getRequestTimeout();
        builder.connectTimeout(timeout, TimeUnit.SECONDS)
               .readTimeout(timeout, TimeUnit.SECONDS)
               .writeTimeout(timeout, TimeUnit.SECONDS);
        
        // 2. 添加重试拦截器
        int maxRetries = apiConfig.getMaxRetries();
        if (maxRetries > 0) {
            builder.addInterceptor(new RetryInterceptor(maxRetries, 1000));
        }
        
        // 3. 添加日志拦截器
        if (apiConfig.isDebugLogging()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                    android.util.Log.d(TAG, message)
            );
            logging.level(HttpLoggingInterceptor.Level.BODY);
            builder.addNetworkInterceptor(logging);
        }
        
        // 4. 添加请求头拦截器（API Key 等）
        builder.addInterceptor(chain -> {
            okhttp3.Request originalRequest = chain.request();
            okhttp3.Request.Builder requestBuilder = originalRequest.newBuilder()
                    .header("User-Agent", apiConfig.getUserAgent())
                    .header("Content-Type", "application/json");
            
            // 添加 API Key（如果配置）
            String apiKey = apiConfig.getApiKey();
            if (!apiKey.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + apiKey);
            }
            
            return chain.proceed(requestBuilder.build());
        });
        
        return builder.build();
    }

    // ========== 简单方法（向下兼容） ==========
    
    /**
     * 发送聊天消息 - 简单版（仅返回回复文本）
     * 
     * @param content 用户消息
     * @param petId 宠物 ID
     * @param callback 回调接口
     */
    public static void sendChatMessage(String content, long petId, ChatCallback callback) {
        sendChatMessage(content, petId, null, null, callback);
    }

    /**
     * 发送聊天消息 - 标准版（包含宠物信息）
     * 根据 API 提供商选择合适的请求格式
     * 
     * @param content 用户消息
     * @param petId 宠物 ID
     * @param petInfo 宠物信息（可null）
     * @param callback 回调接口
     */
    public static void sendChatMessage(String content, long petId, 
                                      @Nullable ChatRequest.PetInfo petInfo, 
                                      ChatCallback callback) {
        sendChatMessage(content, petId, petInfo, null, callback);
    }

    /**
     * 发送聊天消息 - 包含系统提示词版本
     * 
     * @param content 用户消息
     * @param petId 宠物 ID
     * @param petInfo 宠物信息（可null）
     * @param systemPrompt 系统提示词
     * @param callback 回调接口
     */
    public static void sendChatMessage(String content, long petId, 
                                      @Nullable ChatRequest.PetInfo petInfo,
                                      @Nullable String systemPrompt,
                                      ChatCallback callback) {
        String normalizedContent = validateAndNormalizeContent(content, callback);
        if (normalizedContent == null) {
            return;
        }

        if (!validateApiConfig(callback)) {
            return;
        }

        NetworkChatRouter.routeChatMessage(
            getApiService(),
            apiConfig,
            normalizedContent,
            petId,
            petInfo,
            systemPrompt,
            callback,
            ApiClient::logDebug
        );
    }

    private static String validateAndNormalizeContent(String content, @Nullable ChatCallback callback) {
        if (content == null || content.trim().isEmpty()) {
            notifyChatFailure(callback, "消息内容不能为空");
            return null;
        }
        return content.trim();
    }

    private static String validateAndNormalizeContent(String content, @Nullable ChatResponseCallback callback) {
        if (content == null || content.trim().isEmpty()) {
            notifyFullResponseFailure(callback, "消息内容不能为空");
            return null;
        }
        return content.trim();
    }

    private static boolean validateApiConfig(@Nullable ChatCallback callback) {
        if (apiConfig.isConfigValid()) {
            return true;
        }
        notifyChatFailure(callback, "API 配置错误: " + apiConfig.getValidationError());
        return false;
    }

    private static boolean validateApiConfig(@Nullable ChatResponseCallback callback) {
        if (apiConfig.isConfigValid()) {
            return true;
        }
        notifyFullResponseFailure(callback, "API 配置错误: " + apiConfig.getValidationError());
        return false;
    }

    private static void notifyChatFailure(@Nullable ChatCallback callback, String message) {
        if (callback != null) {
            callback.onFailure(message);
        }
    }

    private static void notifyFullResponseFailure(@Nullable ChatResponseCallback callback, String message) {
        if (callback != null) {
            callback.onFailure(message);
        }
    }

    /**
     * 发送聊天消息 - 完整版（获取完整响应，包括 token 统计）
     * 
     * @param content 用户消息
     * @param petId 宠物 ID
     * @param petInfo 宠物信息
     * @param historyMessages 对话历史
     * @param callback 完整响应回调
     */
    public static void sendChatMessageWithFullResponse(
            String content, long petId,
            @Nullable ChatRequest.PetInfo petInfo,
            @Nullable List<ChatRequest.Message> historyMessages,
            ChatResponseCallback callback) {
        String normalizedContent = validateAndNormalizeContent(content, callback);
        if (normalizedContent == null) {
            return;
        }

        if (!validateApiConfig(callback)) {
            return;
        }

        NetworkChatRouter.routeFullResponse(
                getApiService(),
                apiConfig,
                normalizedContent,
                petId,
                petInfo,
                historyMessages,
                callback,
                ApiClient::logDebug
        );
    }

    /**
     * 同步发送聊天消息（用于测试，不建议在 UI 线程调用）
     * 
     * @return 包含回复内容的响应对象
     * @throws Exception 网络异常
     */
    public static ChatResponse sendChatMessageSync(String content, long petId) throws Exception {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        ChatRequest request = new ChatRequest(content.trim(), petId);
        Response<ChatResponse> response = getApiService().sendMessage(request).execute();
        
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        } else {
            throw new Exception("HTTP " + response.code());
        }
    }

    // ========== 工具方法 ==========
    
    /**
     * 打印调试日志
     */
    private static void logDebug(String message) {
        if (apiConfig.isDebugLogging()) {
            android.util.Log.d(TAG, message);
        }
    }

    /**
     * 获取当前 API 配置信息
     */
    public static String getConfigInfo() {
        return apiConfig.getConfigSummary();
    }

    /**
     * 切换 API 提供商
     */
    public static void switchProvider(ApiConfig.ApiProvider provider) {
        apiConfig.setProvider(provider);
        reinitializeApiService();
    }

    /**
     * 配置 OpenAI
     */
    public static void configureOpenAI(String apiKey, String model) {
        apiConfig.configureOpenAI(apiKey, model);
        reinitializeApiService();
    }

    /**
     * 配置本地后端
     */
    public static void configureLocalBackend(String localUrl) {
        apiConfig.configureLocalBackend(localUrl);
        reinitializeApiService();
    }

    /**
     * 配置豆包 (ByteDance Doubao) API
     * 
     * @param apiUrl 豆包 API 端点（如：https://ark.cn-beijing.volces.com/api/v3）
     * @param apiKey Bearer Token（豆包 API 密钥）
     * @param model 模型 ID，例如：doubao-seed-2-0-lite-260215
     */
    public static void configureDoubao(String apiUrl, String apiKey, String model) {
        apiConfig.configureDoubao(apiUrl, apiKey, model != null ? model : "doubao-seed-2-0-lite-260215");
        reinitializeApiService();
        
        // 重新初始化官方豆包 SDK 客户端
        DoubaoApiClient.reinitialize();
        logDebug("豆包客户端已重新初始化 - URL: " + apiUrl + ", 模型: " + model);
    }

    /**
     * 获取 API 配置实例
     */
    public static ApiConfig getApiConfig() {
        return apiConfig;
    }
}
