package com.example.aipet.network;

import androidx.annotation.NonNull;

/**
 * API 配置管理类 - 支持多个 API 提供商和灵活配置
 * 
 * 功能:
 *   • API 端点管理（OpenAI、本地后端、自定义）
 *   • API Key 和授权信息管理
 *   • 网络超时和重试配置
 *   • 运行时切换 API 源
 */
public class ApiConfig {

    /**
     * 支持的 API 提供商枚举
     */
    public enum ApiProvider {
        /**
         * OpenAI - ChatGPT 官方 API
         * Base URL: https://api.openai.com/v1/
         * Endpoint: /chat/completions
         * 需要 API Key
         */
        OPENAI("https://api.openai.com/v1", "/chat/completions", "openai"),

        /**
         * 本地后端服务
         * Base URL: http://10.0.2.2:8080 (模拟器中访问宿主机的地址)
         * Endpoint: /api/chat
         * 仅限开发测试
         */
        LOCAL_BACKEND("http://10.0.2.2:8080", "/api/chat", "local"),

        /**
         * ByteDance Doubao API - 字节跳动豆包
         * Base URL: https://ark.cn-beijing.volces.com/api/v3
         * Endpoint: responses
         * 需要 API Key（Bearer Token）
         * 支持多模态：文本 + 图片
         */
        DOUBAO("https://ark.cn-beijing.volces.com/api/v3", "responses", "doubao"),

        /**
         * 云端自定义 API
         * Base URL: 需要配置
         * Endpoint: /chat
         * 需要 API Key
         */
        CUSTOM("", "/chat", "custom");

        public final String baseUrl;
        public final String endpoint;
        public final String providerName;

        ApiProvider(String baseUrl, String endpoint, String providerName) {
            this.baseUrl = baseUrl;
            this.endpoint = endpoint;
            this.providerName = providerName;
        }

        /**
         * 获取完整 API URL
         */
        public String getFullUrl() {
            return baseUrl + endpoint;
        }

        @Override
        public String toString() {
            return providerName;
        }
    }

    // ========== 单例实例 ==========
    private static volatile ApiConfig instance;
    private static final Object lock = new Object();

    // ========== 配置字段 ==========
    private ApiProvider currentProvider = ApiProvider.DOUBAO;  // 默认豆包 API（ByteDance Doubao）
    private String customBaseUrl = "";  // 自定义 API 基础 URL（用于 CUSTOM 提供商）
    private String doubaoBaseUrl = "";  // 豆包自定义基础 URL（用于 DOUBAO 提供商的自定义端点）
    private String apiKey = "";  // API Key 或 Bearer Token
    private String apiModel = "doubao-seed-2-0-lite-260215";  // 默认豆包模型
    private int requestTimeoutSeconds = 60;      // 网络超时（秒）- 增加到 60 秒以支持豆包等云服务
    private int maxRetries = 5;                  // 最大重试次数 - 增加到 5 次
    private boolean debugLogging = true;         // 是否记录详细日志
    private String userAgent = "AIpet/1.0";      // User-Agent

    // ========== 单例获取 ==========
    public static ApiConfig getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    // ========== 构造方法 ==========
    private ApiConfig() {
        // 初始化默认配置
        initializeDefaults();
    }

    /**
     * 初始化默认配置
     */
    private void initializeDefaults() {
        // 默认使用本地后端（开发模式）
        setProvider(ApiProvider.LOCAL_BACKEND);
        
        // OpenAI API Key（如使用 OpenAI 时需配置）
        // 注意：不建议在代码中硬编码敏感信息，建议使用 BuildConfig 或环境变量
        setApiKey("");
        
        // 默认超时 60 秒（豆包 API 需要更长时间）
        setRequestTimeout(60);
        
        // 默认重试 3 次
        setMaxRetries(3);
    }

    // ========== API 提供商管理 ==========

    /**
     * 获取当前 API 提供商
     */
    public ApiProvider getProvider() {
        return currentProvider;
    }

    /**
     * 设置 API 提供商
     * 
     * @param provider 提供商类型
     */
    public void setProvider(@NonNull ApiProvider provider) {
        this.currentProvider = provider;
        logDebug("API 提供商已切换为: " + provider.providerName);
    }

    /**
     * 获取当前 API 完整 URL
     */
    public String getApiUrl() {
        if (currentProvider == ApiProvider.CUSTOM) {
            return customBaseUrl + ApiProvider.CUSTOM.endpoint;
        }
        if (currentProvider == ApiProvider.DOUBAO && !doubaoBaseUrl.isEmpty()) {
            return doubaoBaseUrl;  // 返回自定义的豆包 URL
        }
        return currentProvider.getFullUrl();
    }

    /**
     * 设置自定义 API 基础 URL（仅 CUSTOM 提供商使用）
     * 
     * @param customUrl 如 "https://your-api.com/v1"
     */
    public void setCustomBaseUrl(@NonNull String customUrl) {
        this.customBaseUrl = customUrl;
        logDebug("自定义 API URL: " + customUrl);
    }

    /**
     * 设置豆包自定义 API 基础 URL（用于自定义豆包端点）
     * 
     * @param doubaoUrl 如 "https://ark.cn-beijing.volces.com/api/v3"
     */
    public void setDoubaoBaseUrl(@NonNull String doubaoUrl) {
        this.doubaoBaseUrl = doubaoUrl;
        logDebug("豆包自定义 API URL: " + doubaoUrl);
    }

    // ========== API 授权配置 ==========

    /**
     * 获取 API Key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 设置 API Key
     * 
     * @param apiKey API 授权 Key（从 OpenAI 或其他服务获取）
     */
    public void setApiKey(@NonNull String apiKey) {
        this.apiKey = apiKey;
        logDebug("API Key 已设置");
    }

    /**
     * 获取 OpenAI 模型名称
     */
    public String getApiModel() {
        return apiModel;
    }

    /**
     * 获取模型名称（别名方法，与 getApiModel() 相同）
     */
    public String getModel() {
        return apiModel;
    }

    /**
     * 设置 OpenAI 模型（如 gpt-3.5-turbo、gpt-4）
     */
    public void setApiModel(@NonNull String model) {
        this.apiModel = model;
        logDebug("模型已切换为: " + model);
    }

    /**
     * 获取 User-Agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置 User-Agent
     */
    public void setUserAgent(@NonNull String agent) {
        this.userAgent = agent;
    }

    // ========== 网络配置 ==========

    /**
     * 获取请求超时时间（秒）
     */
    public int getRequestTimeout() {
        return requestTimeoutSeconds;
    }

    /**
     * 设置请求超时时间
     * 
     * @param seconds 超时秒数（建议 20-60）
     */
    public void setRequestTimeout(int seconds) {
        if (seconds > 0) {
            this.requestTimeoutSeconds = seconds;
            logDebug("请求超时设置: " + seconds + " 秒");
        }
    }

    /**
     * 获取最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * 设置最大重试次数
     * 
     * @param retries 重试次数（建议 0-5）
     */
    public void setMaxRetries(int retries) {
        if (retries >= 0) {
            this.maxRetries = retries;
            logDebug("最大重试次数设置: " + retries);
        }
    }

    // ========== 调试模式 ==========

    /**
     * 是否启用调试日志
     */
    public boolean isDebugLogging() {
        return debugLogging;
    }

    /**
     * 设置调试日志开关
     */
    public void setDebugLogging(boolean enabled) {
        this.debugLogging = enabled;
    }

    /**
     * 打印调试日志
     */
    private void logDebug(String message) {
        if (debugLogging) {
            android.util.Log.d("ApiConfig", message);
        }
    }

    // ========== 配置验证 ==========

    /**
     * 验证当前配置是否完整
     * 
     * @return true 如果配置有效
     */
    public boolean isConfigValid() {
        switch (currentProvider) {
            case OPENAI:
                // OpenAI 需要 API Key
                return !apiKey.isEmpty() && !apiModel.isEmpty();
            
            case DOUBAO:
                // 豆包需要 API Key 和 Model
                return !apiKey.isEmpty() && !apiModel.isEmpty();
            
            case LOCAL_BACKEND:
                // 本地后端无特殊要求
                return true;
            
            case CUSTOM:
                // 自定义需要配置 URL
                return !customBaseUrl.isEmpty();
            
            default:
                return false;
        }
    }

    /**
     * 获取配置验证错误消息
     */
    public String getValidationError() {
        if (!isConfigValid()) {
            switch (currentProvider) {
                case OPENAI:
                    if (apiKey.isEmpty()) {
                        return "OpenAI API Key 未配置";
                    }
                    if (apiModel.isEmpty()) {
                        return "OpenAI 模型未设置";
                    }
                    break;
                
                case DOUBAO:
                    if (apiKey.isEmpty()) {
                        return "豆包 API Key 未配置";
                    }
                    if (apiModel.isEmpty()) {
                        return "豆包模型未设置";
                    }
                    break;
                
                case CUSTOM:
                    if (customBaseUrl.isEmpty()) {
                        return "自定义 API URL 未配置";
                    }
                    break;
            }
        }
        return "";
    }

    // ========== 配置导出 ==========

    /**
     * 获取当前配置摘要（用于日志和调试）
     */
    public String getConfigSummary() {
        return String.format(
            "API 配置摘要:\n" +
            "  提供商: %s\n" +
            "  URL: %s\n" +
            "  模型: %s\n" +
            "  超时: %d 秒\n" +
            "  重试: %d 次\n" +
            "  调试: %s",
            currentProvider.providerName,
            getApiUrl(),
            apiModel.isEmpty() ? "N/A" : apiModel,
            requestTimeoutSeconds,
            maxRetries,
            debugLogging ? "是" : "否"
        );
    }

    /**
     * 重置为默认配置
     */
    public void resetToDefaults() {
        initializeDefaults();
        logDebug("配置已重置为默认值");
    }

    // ========== 建议的配置方案 ==========

    /**
     * 配置为 OpenAI 模式
     * 
     * @param apiKey OpenAI API Key
     * @param model  模型名称（如 gpt-3.5-turbo、gpt-4）
     */
    public void configureOpenAI(@NonNull String apiKey, @NonNull String model) {
        setProvider(ApiProvider.OPENAI);
        setApiKey(apiKey);
        setApiModel(model);
        logDebug("已配置 OpenAI: " + model);
    }

    /**
     * 配置为本地后端模式（开发/测试）
     * 
     * @param localUrl 本地服务器地址，如 "http://localhost:8080"
     */
    public void configureLocalBackend(@NonNull String localUrl) {
        setProvider(ApiProvider.LOCAL_BACKEND);
        setRequestTimeout(15);  // 本地连接超时稍长（模拟器 DNS 查询延迟）
        logDebug("已配置本地后端: " + localUrl);
    }

    /**
     * 配置为自定义 API 模式
     * 
     * @param baseUrl 自定义 API 基础 URL
     * @param apiKey  API Key（如需要）
     */
    public void configureCustom(@NonNull String baseUrl, @NonNull String apiKey) {
        setProvider(ApiProvider.CUSTOM);
        setCustomBaseUrl(baseUrl);
        setApiKey(apiKey);
        logDebug("已配置自定义 API: " + baseUrl);
    }

    /**
     * 配置为自定义 API 模式（带模型名称）
     * 
     * @param baseUrl   自定义 API 基础 URL
     * @param apiKey    API Key（如需要）
     * @param modelName 模型名称
     */
    public void configureCustom(@NonNull String baseUrl, @NonNull String apiKey, @NonNull String modelName) {
        configureCustom(baseUrl, apiKey);
        setApiModel(modelName);
    }

    /**
     * 配置为豆包 (ByteDance Doubao) 模式
     * 
     * @param apiKey Bearer Token（豆包 API 密钥）
     * @param model  豆包模型名称（如 doubao-seed-2-0-lite-260215）
     */
    public void configureDoubao(@NonNull String apiUrl, @NonNull String apiKey, @NonNull String model) {
        setProvider(ApiProvider.DOUBAO);
        if (!apiUrl.isEmpty()) {
            setDoubaoBaseUrl(apiUrl);  // 设置自定义的豆包 URL
        }
        setApiKey(apiKey);
        setApiModel(model);
        logDebug("已配置豆包: " + model + " (URL: " + (!apiUrl.isEmpty() ? apiUrl : "官方") + ")");
    }
}
