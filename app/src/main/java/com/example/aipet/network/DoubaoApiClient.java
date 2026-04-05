package com.example.aipet.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.service.ArkService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 豆包官方 SDK 客户端
 * 
 * 功能：
 *   • 使用 volcengine-java-sdk-ark-runtime 官方 SDK
 *   • 支持自定义 API URL 和 Key
 *   • 完整的错误处理和日志
 *   • 与应用现有回调接口兼容
 */
public class DoubaoApiClient {

    private static final String TAG = "DoubaoApiClient";
    private static final int REQUEST_EXECUTOR_THREADS = 2;
    private static ArkService arkService;
    private static ExecutorService requestExecutor = createRequestExecutor();
    private static final Object lock = new Object();
    private static final ApiConfig apiConfig = ApiConfig.getInstance();

    private static ExecutorService createRequestExecutor() {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "doubao-request-worker");
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(REQUEST_EXECUTOR_THREADS, threadFactory);
    }

    private static ExecutorService getRequestExecutor() {
        synchronized (lock) {
            if (requestExecutor == null || requestExecutor.isShutdown()) {
                requestExecutor = createRequestExecutor();
            }
            return requestExecutor;
        }
    }

    /**
     * 获取或创建 ArkService 实例
     */
    private static ArkService getArkService() {
        if (arkService == null) {
            synchronized (lock) {
                if (arkService == null) {
                    initializeArkService();
                }
            }
        }
        return arkService;
    }

    /**
     * 初始化 ArkService
     */
    private static void initializeArkService() {
        try {
            String apiKey = apiConfig.getApiKey();
            String apiUrl = apiConfig.getApiUrl();
            
            if (apiKey == null || apiKey.isEmpty()) {
                Log.e(TAG, "API Key 未配置");
                return;
            }
            
            Log.d(TAG, "初始化 ArkService - URL: " + apiUrl + ", Key: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
            
            arkService = ArkService.builder()
                    .apiKey(apiKey)
                    .baseUrl(apiUrl)
                    .build();
                    
            Log.d(TAG, "ArkService 初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "ArkService 初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重新初始化 ArkService（配置变更时调用）
     */
    public static void reinitialize() {
        synchronized (lock) {
            if (arkService != null) {
                try {
                    arkService.shutdownExecutor();
                } catch (Exception e) {
                    Log.w(TAG, "关闭旧 ArkService 时出错: " + e.getMessage());
                }
            }
            arkService = null;
        }
    }

    /**
     * 发送聊天消息到豆包 API
     * 
     * @param content 用户消息
     * @param callback 回调接口
     */
    public static void sendMessage(String content, @NonNull ApiClient.ChatCallback callback) {
        sendMessage(content, null, callback);
    }

    /**
     * 发送聊天消息到豆包 API（包含系统提示词）
     * 
     * @param content 用户消息
     * @param systemPrompt 系统提示词（可null）
     * @param callback 回调接口
     */
    public static void sendMessage(String content, @Nullable String systemPrompt, 
                                   @NonNull ApiClient.ChatCallback callback) {
        // 验证输入
        if (content == null || content.trim().isEmpty()) {
            callback.onFailure("消息内容不能为空");
            return;
        }

        // 验证 API 配置
        if (!apiConfig.isConfigValid()) {
            callback.onFailure("豆包 API 配置错误: " + apiConfig.getValidationError());
            return;
        }

        // 获取 ArkService
        ArkService service = getArkService();
        if (service == null) {
            callback.onFailure("豆包 API 客户端初始化失败");
            return;
        }

        try {
            String promptPayload = buildPromptPayload(content.trim(), systemPrompt);

            // 构建请求
            CreateResponsesRequest.Builder requestBuilder = CreateResponsesRequest.builder()
                    .model(apiConfig.getModel())
                    .input(ResponsesInput.builder()
                            .stringValue(promptPayload)
                            .build());

            CreateResponsesRequest request = requestBuilder.build();

            Log.d(TAG, "发送消息到豆包 - 模型: " + apiConfig.getModel() + 
                    ", 系统提示: " + (systemPrompt != null && !systemPrompt.trim().isEmpty()) +
                    ", 内容: " + content.substring(0, Math.min(30, content.length())));

            // 执行请求（同步调用在线程池中）
            getRequestExecutor().execute(() -> {
                try {
                    ResponseObject response = service.createResponse(request);
                    
                    Log.d(TAG, "收到豆包响应 - 状态: " + (response != null ? "Success" : "Null"));
                    
                    if (response != null && response.getOutput() != null) {
                        String reply = response.getOutput().toString();
                        if (!reply.isEmpty()) {
                            Log.d(TAG, "豆包回复: " + reply.substring(0, Math.min(50, reply.length())));
                            callback.onSuccess(reply);
                        } else {
                            callback.onFailure("豆包返回空回复");
                        }
                    } else {
                        callback.onFailure("豆包返回无效响应");
                    }
                } catch (Exception e) {
                    String errorMsg = "豆包 API 错误: " + e.getMessage();
                    Log.e(TAG, errorMsg, e);
                    callback.onFailure(errorMsg);
                }
            });

        } catch (Exception e) {
            String errorMsg = "构建豆包请求失败: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            callback.onFailure(errorMsg);
        }
    }

    private static String buildPromptPayload(@NonNull String userContent, @Nullable String systemPrompt) {
        if (systemPrompt == null || systemPrompt.trim().isEmpty()) {
            return userContent;
        }

        String cleanedSystemPrompt = systemPrompt.trim();
        return "系统设定：\n" + cleanedSystemPrompt + "\n\n用户消息：\n" + userContent;
    }

    /**
     * 关闭客户端资源
     */
    public static void shutdown() {
        synchronized (lock) {
            if (arkService != null) {
                try {
                    arkService.shutdownExecutor();
                    Log.d(TAG, "ArkService 已关闭");
                } catch (Exception e) {
                    Log.e(TAG, "关闭 ArkService 时出错: " + e.getMessage());
                }
                arkService = null;
            }
        }
        synchronized (lock) {
            if (requestExecutor != null && !requestExecutor.isShutdown()) {
                requestExecutor.shutdown();
            }
        }
    }
}
