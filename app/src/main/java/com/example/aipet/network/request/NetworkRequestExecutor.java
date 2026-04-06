package com.example.aipet.network.request;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aipet.network.ApiClient;
import com.example.aipet.network.ChatResponse;

import java.util.Locale;
import java.util.function.Consumer;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 网络请求通用执行器
 * 
 * 统一处理所有 Retrofit 网络请求的回调、错误处理、日志记录等重复逻辑，
 * 避免在每个网络请求中重复相同的代码
 */
public class NetworkRequestExecutor {
    
    private static final String TAG = "NetworkRequest";
    private static final int DEFAULT_ERROR_SNIPPET_LENGTH = 100;

    private static String readErrorBody(@NonNull Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                return errorBody.string();
            }
        } catch (Exception e) {
            return e.getMessage() != null ? e.getMessage() : "";
        }
        return "";
    }

    private static String safeMessage(@Nullable Throwable throwable, @NonNull String fallback) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().isEmpty()) {
            return fallback;
        }
        return throwable.getMessage();
    }

    private static String abbreviate(@Nullable String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.substring(0, Math.min(DEFAULT_ERROR_SNIPPET_LENGTH, content.length()));
    }

    private static String buildHttpError(int statusCode, @Nullable String errorBody) {
        String shortBody = abbreviate(errorBody);
        return shortBody.isEmpty()
                ? String.format(Locale.ROOT, "HTTP %d", statusCode)
                : String.format(Locale.ROOT, "HTTP %d: %s", statusCode, shortBody);
    }
    
    /**
     * 通用网络请求执行方法
     * 
     * 处理所有的回调逻辑、错误处理、日志记录
     * 
     * @param <T> 响应体类型
     * @param call Retrofit 的 Call 对象
     * @param apiName API 名称（用于日志）
     * @param responseHandler 响应处理器
     */
    public static <T> void execute(@NonNull Call<T> call, 
                                   @NonNull String apiName,
                                   @NonNull ApiResponseHandler<T> responseHandler) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, apiName + " - 响应成功");
                    responseHandler.onSuccess(response.body());
                } else {
                    int statusCode = response.code();
                    String errorBody = readErrorBody(response);
                    String errorMsg = buildHttpError(statusCode, errorBody);

                    Log.w(TAG, apiName + " - 请求失败: " + errorMsg);
                    responseHandler.onHttpError(statusCode, errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                String errorMessage = safeMessage(t, "未知网络错误");
                Log.e(TAG, apiName + " - 请求异常: " + errorMessage, t);
                responseHandler.onNetworkError(errorMessage);
            }
        });
    }
    
    /**
     * 带 JSON 检查的通用请求执行方法
     * 
     * 如果响应被解析为空或不符合预期，会调用错误处理
     * 
     * @param <T> 响应体类型
     * @param call Retrofit 的 Call 对象
     * @param apiName API 名称
     * @param responseValidator 响应验证器
     * @param responseHandler 响应处理器
     */
    public static <T> void executeWithValidation(@NonNull Call<T> call,
                                                 @NonNull String apiName,
                                                 @NonNull ResponseValidator<T> responseValidator,
                                                 @NonNull ApiResponseHandler<T> responseHandler) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    T body = response.body();
                    
                    // 验证响应内容
                    if (responseValidator.isValid(body)) {
                        Log.d(TAG, apiName + " - 响应验证成功");
                        responseHandler.onSuccess(body);
                    } else {
                        Log.w(TAG, apiName + " - 响应验证失败");
                        responseHandler.onValidationError("响应格式不符合预期");
                    }
                } else {
                    int statusCode = response.code();
                    String errorBody = readErrorBody(response);
                    String errorMsg = buildHttpError(statusCode, errorBody);
                    Log.w(TAG, apiName + " - 请求失败: " + errorMsg);
                    responseHandler.onHttpError(statusCode, errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                String errorMessage = safeMessage(t, "未知网络错误");
                Log.e(TAG, apiName + " - 请求异常: " + errorMessage, t);
                responseHandler.onNetworkError(errorMessage);
            }
        });
    }
    
    /**
     * API 响应处理器接口
     */
    public interface ApiResponseHandler<T> {
        /**
         * 请求成功且响应有效
         */
        void onSuccess(T response);
        
        /**
         * HTTP 错误（非 2xx 状态码）
         */
        void onHttpError(int statusCode, String errorBody);
        
        /**
         * 网络错误（无法连接等）
         */
        void onNetworkError(String message);
        
        /**
         * 响应验证失败（仅当使用 executeWithValidation 时调用）
         */
        default void onValidationError(String message) {
            onNetworkError("验证失败: " + message);
        }
    }
    
    /**
     * 响应验证器接口
     */
    public interface ResponseValidator<T> {
        /**
         * 验证响应是否有效
         */
        boolean isValid(T response);
    }
    
    /**
     * 执行聊天请求 - 用于 ChatCallback
     * 
     * 统一处理 ChatResponse 的解析和回调
     * 
     * @param call Retrofit 的 Call 对象
     * @param callback 聊天回调
     * @param apiName API 名称
     * @param logger 日志器
     */
    public static void executeChatRequest(
            @NonNull Call<ChatResponse> call,
            @Nullable ApiClient.ChatCallback callback,
            @NonNull String apiName,
            @NonNull Consumer<String> logger) {
        
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call,
                                 @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    String reply = chatResponse.getReplyContent();
                    
                    if (reply != null && !reply.isEmpty()) {
                        logger.accept(apiName + " 收到回复: " + reply.substring(0, Math.min(50, reply.length())));
                        if (callback != null) {
                            callback.onSuccess(reply);
                        }
                    } else {
                        String errorMsg = apiName + " 返回空回复";
                        logger.accept(errorMsg);
                        if (callback != null) {
                            callback.onFailure(errorMsg);
                        }
                    }
                } else {
                    int statusCode = response.code();
                    String errorBody = readErrorBody(response);
                    String errorMsg = apiName + " API 错误 " + buildHttpError(statusCode, errorBody);
                    
                    logger.accept(errorMsg);
                    if (callback != null) {
                        callback.onFailure(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                String errorMessage = safeMessage(t, "未知网络错误");
                logger.accept(apiName + " 请求异常: " + errorMessage);
                
                if (callback != null) {
                    callback.onFailure(errorMessage);
                }
            }
        });
    }
    
    /**
     * 执行完整响应请求 - 用于 ChatResponseCallback
     * 
     * 返回完整的 ChatResponse 对象，包括 Token 统计等信息
     * 
     * @param call Retrofit 的 Call 对象
     * @param callback 完整响应回调
     * @param logger 日志器
     */
    public static void executeFullResponse(
            @NonNull Call<ChatResponse> call,
            @Nullable ApiClient.ChatResponseCallback callback,
            @NonNull Consumer<String> logger) {
        
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call,
                                 @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    logger.accept("完整响应成功: Tokens=" + chatResponse.getTotalTokens());
                    
                    if (callback != null) {
                        callback.onSuccess(chatResponse);
                    }
                } else {
                    int statusCode = response.code();
                    String errorBody = readErrorBody(response);
                    String errorMsg = buildHttpError(statusCode, errorBody);
                    logger.accept("完整响应请求失败: " + errorMsg);
                    
                    if (callback != null) {
                        callback.onFailure(errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                String errorMessage = safeMessage(t, "网络请求失败");
                logger.accept("完整响应请求异常: " + errorMessage);
                
                if (callback != null) {
                    callback.onFailure(errorMessage);
                }
            }
        });
    }
}
