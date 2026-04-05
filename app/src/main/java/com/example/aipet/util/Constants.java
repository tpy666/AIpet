package com.example.aipet.util;

/**
 * 应用全局常量集中管理
 * 
 * 将所有硬编码的 API URL、SharedPreferences 键名、日志标签等集中定义在此，
 * 便于统一维护和修改。
 */
public class Constants {
    
    // ============ SharedPreferences 相关 ============
    
    /**
     * API 配置的 SharedPreferences 文件名
     */
    public static final String SP_API_SETTINGS = "api_settings";

    /**
     * 通用业务 SharedPreferences 文件名
     */
    public static final String SP_APP_PREFS = "aipet_pref";
    
    /**
     * 宠物列表的 SharedPreferences 键名
     */
    public static final String KEY_PET_LIST = "pet_list";
    
    // ============ API 配置键名 ============
    
    /**
     * API 提供商键名（openai | doubao | local | custom）
     */
    public static final String KEY_API_PROVIDER = "api_provider";
    
    /**
     * API 地址键名
     */
    public static final String KEY_API_URL = "api_url";
    
    /**
     * API 密钥键名
     */
    public static final String KEY_API_KEY = "api_key";
    
    /**
     * 模型名称键名
     */
    public static final String KEY_MODEL_NAME = "model_name";
    
    // ============ API 端点 URL ============
    
    /**
     * OpenAI API 基础 URL
     */
    public static final String OPENAI_BASE_URL = "https://api.openai.com/v1";
    
    /**
     * 豆包 API 基础 URL
     */
    public static final String DOUBAO_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";
    
    /**
     * 本地后端默认 URL（模拟器中访问宿主机）
     */
    public static final String LOCAL_BACKEND_URL = "http://10.0.2.2:8080";
    
    /**
     * 自定义 API 示例 URL
     */
    public static final String CUSTOM_API_EXAMPLE_URL = "https://your-api-server.com/v1";
    
    // ============ 默认模型名称 ============
    
    /**
     * OpenAI 默认模型
     */
    public static final String OPENAI_DEFAULT_MODEL = "gpt-3.5-turbo";
    
    /**
     * 豆包默认模型
     */
    public static final String DOUBAO_DEFAULT_MODEL = "doubao-seed-2-0-lite-260215";
    
    /**
     * 本地后端默认模型名称
     */
    public static final String LOCAL_BACKEND_DEFAULT_MODEL = "local";
    
    // ============ 日志相关常量 ============
    
    /**
     * 日志目录名
     */
    public static final String LOG_DIR_NAME = "chat_logs";
    
    /**
     * 日志文件前缀
     */
    public static final String LOG_FILE_PREFIX = "chat_";
    
    /**
     * 日志文件扩展名
     */
    public static final String LOG_FILE_EXTENSION = ".log";
    
    // ============ 日志标签 ============
    
    /**
     * 用户消息日志标签
     */
    public static final String LOG_TAG_USER_MESSAGE = "USER_MESSAGE";
    
    /**
     * 宠物回复日志标签
     */
    public static final String LOG_TAG_PET_REPLY = "PET_REPLY";
    
    /**
     * API 请求日志标签
     */
    public static final String LOG_TAG_API_REQUEST = "API_REQUEST";
    
    /**
     * API 响应日志标签
     */
    public static final String LOG_TAG_API_RESPONSE = "API_RESPONSE";
    
    /**
     * API 错误日志标签
     */
    public static final String LOG_TAG_API_ERROR = "API_ERROR";
    
    /**
     * 降级使用日志标签
     */
    public static final String LOG_TAG_FALLBACK = "FALLBACK";
    
    // ============ HTTP 请求相关 ============
    
    /**
     * HTTP 连接超时（秒）
     */
    public static final long HTTP_CONNECT_TIMEOUT = 10;
    
    /**
     * HTTP 读取超时（秒）
     */
    public static final long HTTP_READ_TIMEOUT = 10;
    
    /**
     * HTTP 方法 POST
     */
    public static final String HTTP_METHOD_POST = "POST";
    
    /**
     * HTTP 方法 GET
     */
    public static final String HTTP_METHOD_GET = "GET";
    
    /**
     * JSON 内容类型
     */
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    
    // ============ API 提供商标识 ============
    
    /**
     * OpenAI 提供商标识
     */
    public static final String PROVIDER_OPENAI = "openai";
    
    /**
     * 豆包提供商标识
     */
    public static final String PROVIDER_DOUBAO = "doubao";
    
    /**
     * 本地后端提供商标识
     */
    public static final String PROVIDER_LOCAL = "local";
    
    /**
     * 自定义提供商标识
     */
    public static final String PROVIDER_CUSTOM = "custom";
    
    // ============ 杂项常量 ============
    
    /**
     * 启动页面延迟时间（毫秒）
     */
    public static final long SPLASH_DELAY = 2000;
    
    /**
     * 日志最大内存条数
     */
    public static final int MAX_MEMORY_LOGS = 1000;
}
