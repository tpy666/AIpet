package com.example.aipet.network;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp 自动重试拦截器
 * 
 * 功能：
 *   • 网络错误自动重试（最多 N 次）
 *   • 可配置的退避延迟（1s, 2s, 4s...）
 *   • 智能判断可重试错误
 *   • 记录重试日志
 */
public class RetryInterceptor implements Interceptor {

    private static final String TAG = "RetryInterceptor";
    private static final int DEFAULT_MAX_RETRY = 3;
    
    private final int maxRetry;
    private final long retryDelayMillis;
    private final ApiConfig apiConfig;

    public RetryInterceptor() {
        this(DEFAULT_MAX_RETRY, 1000);
    }

    public RetryInterceptor(int maxRetry, long retryDelayMillis) {
        this.maxRetry = maxRetry;
        this.retryDelayMillis = retryDelayMillis;
        this.apiConfig = ApiConfig.getInstance();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        IOException lastException = null;
        
        for (int i = 0; i <= maxRetry; i++) {
            try {
                Response response = chain.proceed(request);
                
                // 判断是否需要重试 (5xx 服务器错误)
                if (!response.isSuccessful() && response.code() >= 500 && i < maxRetry) {
                    response.close();
                    logRetry(i + 1, "HTTP " + response.code() + " 服务器错误，准备重试");
                    delay(calculateDelayMillis(i));
                    continue;
                }
                
                return response;
                
            } catch (IOException e) {
                lastException = e;
                
                // 判断是否为可重试错误
                if (isRetryableError(e) && i < maxRetry) {
                    logRetry(i + 1, "网络错误: " + e.getMessage());
                    delay(calculateDelayMillis(i));
                    continue;
                }
                
                // 不可重试错误，直接抛出
                throw e;
            }
        }
        
        // 所有重试均失败，抛出最后一个异常
        if (lastException != null) {
            throw lastException;
        }
        
        throw new IOException("请求失败：已重试 " + maxRetry + " 次");
    }

    /**
     * 判断错误是否可重试
     * 
     * 可重试错误：
     *   • SocketTimeoutException - 连接超时
     *   • ConnectException - 连接失败
     *   • SocketException - Socket 异常
     *   • 暂时性网络故障
     * 
     * 不可重试错误：
     *   • 4xx 客户端错误（除了 408/429）
     *   • SSL 证书错误
     *   • 协议错误
     */
    private boolean isRetryableError(IOException exception) {
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage == null) {
            exceptionMessage = "";
        }
        
        // 可重试的异常类型
        if (exception instanceof java.net.SocketTimeoutException) {
            return true;  // 连接超时
        }
        
        if (exception instanceof java.net.ConnectException) {
            return true;  // 连接失败
        }
        
        if (exception instanceof java.net.SocketException) {
            return true;  // Socket 错误
        }
        
        // 判断异常消息内容
        if (exceptionMessage.contains("Temporary failure in name resolution") ||
            exceptionMessage.contains("Network unreachable") ||
            exceptionMessage.contains("Connection reset") ||
            exceptionMessage.contains("Connection refused") ||
            exceptionMessage.contains("timeout")) {
            return true;
        }
        
        return false;
    }

    /**
     * 计算退避延迟时间（指数级）
     * 
     * 重试延迟序列：
     *   第 1 次重试: 1 秒
     *   第 2 次重试: 2 秒
     *   第 3 次重试: 4 秒
     *   第 N 次重试: 2^(N-1) 秒 * 基础延迟
     */
    private long calculateDelayMillis(int attemptNumber) {
        // 指数退避：delay * 2^attemptNumber
        long exponentialDelay = retryDelayMillis * (1L << attemptNumber);
        
        // 加入随机抖动，避免雷鸣羊群问题
        long jitter = (long) (Math.random() * 1000);
        
        // 最大延迟 32 秒
        long maxDelay = 32000;
        return Math.min(exponentialDelay + jitter, maxDelay);
    }

    /**
     * 延迟等待
     */
    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 记录重试日志
     */
    private void logRetry(int attemptNumber, String reason) {
        if (apiConfig.isDebugLogging()) {
            String message = String.format(
                Locale.ROOT,
                "[重试 %d/%d] %s",
                attemptNumber,
                maxRetry,
                reason
            );
            android.util.Log.d(TAG, message);
        }
    }
}
