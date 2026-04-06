package com.example.aipet.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 聊天日志管理器
 * 
 * 功能：
 * • 记录所有 API 请求和响应
 * • 记录消息发送和接收
 * • 支持日志持久化到文件
 * • 支持查看实时日志
 */
public class ChatLogger {
    
    private static final String TAG = "ChatLogger";
    
    private static ChatLogger instance;
    private final Context context;
    private final List<LogEntry> memoryLogs;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat fileNameFormat;
    private String currentSessionId;
    
    // 日志等级
    public static final int LEVEL_DEBUG = 0;
    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_WARNING = 2;
    public static final int LEVEL_ERROR = 3;

    public enum LogLevel {
        DEBUG(LEVEL_DEBUG, "DEBUG"),
        INFO(LEVEL_INFO, "INFO"),
        WARNING(LEVEL_WARNING, "WARN"),
        ERROR(LEVEL_ERROR, "ERROR");

        final int value;
        final String label;

        LogLevel(int value, String label) {
            this.value = value;
            this.label = label;
        }

        static LogLevel fromValue(int value) {
            switch (value) {
                case LEVEL_INFO:
                    return INFO;
                case LEVEL_WARNING:
                    return WARNING;
                case LEVEL_ERROR:
                    return ERROR;
                case LEVEL_DEBUG:
                default:
                    return DEBUG;
            }
        }
    }
    
    private int minLogLevel = LEVEL_DEBUG;
    private boolean enableFileLogging = true;
    
    /**
     * 日志条目数据类
     */
    public static class LogEntry {
        public String timestamp;
        public int level;
        public String tag;
        public String message;
        public String exception;
        
        public LogEntry(String timestamp, int level, String tag, String message, String exception) {
            this.timestamp = timestamp;
            this.level = level;
            this.tag = tag;
            this.message = message;
            this.exception = exception;
        }
        
        @Override
        public String toString() {
            String levelStr = LogLevel.fromValue(level).label;
            
            String result = String.format("[%s] %s - %s: %s", timestamp, levelStr, tag, message);
            if (exception != null && !exception.isEmpty()) {
                result += "\n" + exception;
            }
            return result;
        }
    }
    
    private ChatLogger(Context context) {
        this.context = context.getApplicationContext();
        this.memoryLogs = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        this.fileNameFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.currentSessionId = String.valueOf(System.currentTimeMillis());
        
        ensureLogDirectory();
    }
    
    /**
     * 获取单例
     */
    public static synchronized ChatLogger getInstance(Context context) {
        if (instance == null) {
            instance = new ChatLogger(context);
        }
        return instance;
    }
    
    /**
     * 确保日志目录存在
     */
    private void ensureLogDirectory() {
        try {
            File logDir = new File(context.getFilesDir(), Constants.LOG_DIR_NAME);
            if (!logDir.exists()) {
                boolean created = logDir.mkdirs();
                if (!created) {
                    Log.w(TAG, "Log directory creation failed or already handled: " + logDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create log directory", e);
        }
    }
    
    /**
     * 调试日志
     */
    public void debug(String tag, String message) {
        log(LogLevel.DEBUG, tag, message, null);
    }
    
    /**
     * 信息日志
     */
    public void info(String tag, String message) {
        log(LogLevel.INFO, tag, message, null);
    }
    
    /**
     * 警告日志
     */
    public void warning(String tag, String message) {
        log(LogLevel.WARNING, tag, message, null);
    }
    
    /**
     * 错误日志
     */
    public void error(String tag, String message, Throwable exception) {
        String exceptionStr = exception != null ? Log.getStackTraceString(exception) : "";
        log(LogLevel.ERROR, tag, message, exceptionStr);
    }
    
    /**
     * 错误日志（不含异常）
     */
    public void error(String tag, String message) {
        log(LogLevel.ERROR, tag, message, null);
    }

    /**
     * 统一日志入口（推荐）
     */
    public void log(LogLevel level, String tag, String message) {
        log(level, tag, message, null);
    }

    /**
     * 兼容旧代码的统一入口
     */
    public void log(int level, String tag, String message) {
        log(LogLevel.fromValue(level), tag, message, null);
    }

    private synchronized void log(LogLevel level, String tag, String message, String exception) {
        log(level.value, tag, message, exception);
    }
    
    /**
     * 核心日志方法
     */
    private synchronized void log(int level, String tag, String message, String exception) {
        if (level < minLogLevel) {
            return;
        }
        
        String timestamp = dateFormat.format(new Date());
        LogEntry entry = new LogEntry(timestamp, level, tag, message, exception);
        
        // 添加到内存
        memoryLogs.add(entry);
        if (memoryLogs.size() > Constants.MAX_MEMORY_LOGS) {
            memoryLogs.remove(0);
        }
        
        // 输出到 Logcat
        outputToLogcat(level, tag, message, exception);
        
        // 保存到文件
        if (enableFileLogging) {
            outputToFile(entry);
        }
    }
    
    /**
     * 输出到 Logcat
     */
    private void outputToLogcat(int level, String tag, String message, String exception) {
        LogLevel logLevel = LogLevel.fromValue(level);
        if (logLevel == LogLevel.DEBUG) {
            Log.d(tag, message);
        } else if (logLevel == LogLevel.INFO) {
            Log.i(tag, message);
        } else if (logLevel == LogLevel.WARNING) {
            Log.w(tag, message);
        } else {
            if (exception != null) {
                Log.e(tag, message + "\n" + exception);
            } else {
                Log.e(tag, message);
            }
        }
    }
    
    /**
     * 输出到文件
     */
    private void outputToFile(LogEntry entry) {
        try {
            String fileName = Constants.LOG_FILE_PREFIX + fileNameFormat.format(new Date()) + Constants.LOG_FILE_EXTENSION;
            File logFile = new File(context.getFilesDir(), Constants.LOG_DIR_NAME + File.separator + fileName);

            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(entry.toString() + "\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write log file", e);
        }
    }
    
    /**
     * 获取所有内存日志
     */
    public List<LogEntry> getMemoryLogs() {
        return new ArrayList<>(memoryLogs);
    }
    
    /**
     * 获取最近 N 条日志
     */
    public List<LogEntry> getRecentLogs(int count) {
        List<LogEntry> result = new ArrayList<>();
        int startIndex = Math.max(0, memoryLogs.size() - count);
        for (int i = startIndex; i < memoryLogs.size(); i++) {
            result.add(memoryLogs.get(i));
        }
        return result;
    }
    
    /**
     * 获取特定标签的日志
     */
    public List<LogEntry> getLogsByTag(String tag) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : memoryLogs) {
            if (entry.tag.equals(tag)) {
                result.add(entry);
            }
        }
        return result;
    }
    
    /**
     * 获取特定级别的日志
     */
    public List<LogEntry> getLogsByLevel(int level) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry entry : memoryLogs) {
            if (entry.level == level) {
                result.add(entry);
            }
        }
        return result;
    }
    
    /**
     * 清空日志
     */
    public synchronized void clearMemoryLogs() {
        memoryLogs.clear();
    }
    
    /**
     * 清空所有日志文件
     */
    public void clearAllLogFiles() {
        try {
            File logDir = new File(context.getFilesDir(), Constants.LOG_DIR_NAME);
            if (logDir.exists() && logDir.isDirectory()) {
                File[] files = logDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.delete()) {
                            Log.w(TAG, "Failed to delete log file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear log files", e);
        }
    }
    
    /**
     * 导出日志为字符串
     */
    public String exportLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ChatLogger 日志导出 ===\n");
        sb.append("会话 ID: ").append(currentSessionId).append("\n");
        sb.append("导出时间: ").append(new Date()).append("\n");
        sb.append("日志条数: ").append(memoryLogs.size()).append("\n");
        sb.append("===========================\n\n");
        
        for (LogEntry entry : memoryLogs) {
            sb.append(entry.toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 设置最小日志级别
     */
    public void setMinLogLevel(int level) {
        this.minLogLevel = level;
    }

    public void setMinLogLevel(LogLevel level) {
        this.minLogLevel = level.value;
    }
    
    /**
     * 启用/禁用文件日志
     */
    public void setFileLoggingEnabled(boolean enabled) {
        this.enableFileLogging = enabled;
    }
    
    /**
     * 获取日志文件路径
     */
    public String getLogDirPath() {
        return new File(context.getFilesDir(), Constants.LOG_DIR_NAME).getAbsolutePath();
    }
    
    /**
     * 获取今天的日志文件
     */
    public File getTodayLogFile() {
        String fileName = Constants.LOG_FILE_PREFIX + fileNameFormat.format(new Date()) + Constants.LOG_FILE_EXTENSION;
        return new File(context.getFilesDir(), Constants.LOG_DIR_NAME + File.separator + fileName);
    }
    
    // ============ 特定场景日志方法 ============
    
    /**
     * 记录用户发送的消息
     */
    public void logUserMessage(String text, String petName) {
        info(Constants.LOG_TAG_USER_MESSAGE, "User: " + text + " [Pet: " + petName + "]");
    }
    
    /**
     * 记录 API 请求
     */
    public void logApiRequest(String url, String method, String action) {
        info(Constants.LOG_TAG_API_REQUEST, "Method: " + method + ", URL: " + url + ", Action: " + action);
    }
    
    /**
     * 记录 API 响应
     */
    public void logApiResponse(String response, int statusCode) {
        info(Constants.LOG_TAG_API_RESPONSE, "Status: " + statusCode + ", Response: " + response);
    }
    
    /**
     * 记录宠物回复
     */
    public void logPetReply(String reply, String petName) {
        info(Constants.LOG_TAG_PET_REPLY, "[" + petName + "]: " + reply);
    }
    
    /**
     * 记录 API 错误
     */
    public void logApiError(String errorMsg, String errorCode, String details) {
        error(Constants.LOG_TAG_API_ERROR, errorCode + " - " + errorMsg + " | Details: " + details);
    }
    
    /**
     * 记录降级使用
     */
    public void logFallbackUsage(String reason) {
        warning(Constants.LOG_TAG_FALLBACK, "Using fallback mechanism: " + reason);
    }
    
    /**
     * 获取聊天日志内容
     */
    public String getChatLogContent() {
        return buildTaggedLogContent(
                "=== 聊天日志 ===",
                "暂无聊天日志",
                Constants.LOG_TAG_USER_MESSAGE,
                Constants.LOG_TAG_PET_REPLY,
                Constants.LOG_TAG_API_REQUEST,
                Constants.LOG_TAG_API_RESPONSE
        );
    }
    
    /**
     * 清空所有日志（包括内存和文件）
     */
    public void clearAllLogs() {
        clearMemoryLogs();
        clearAllLogFiles();
    }
    
    /**
     * 获取错误日志内容
     */
    public String getErrorLogContent() {
        return buildTaggedLogContent(
                "=== 错误日志 ===",
                "暂无错误日志",
                Constants.LOG_TAG_API_ERROR,
                Constants.LOG_TAG_FALLBACK
        );
    }
    
    /**
     * 清空错误日志
     */
    public void clearErrorLogs() {
        clearLogsByTags(Constants.LOG_TAG_API_ERROR, Constants.LOG_TAG_FALLBACK);
    }

    private String buildTaggedLogContent(String title, String emptyHint, String... tags) {
        List<LogEntry> logs = collectLogsByTags(tags);
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n");

        for (LogEntry entry : logs) {
            sb.append(entry.toString()).append("\n");
        }

        if (logs.isEmpty()) {
            sb.append(emptyHint);
        }

        return sb.toString();
    }

    private List<LogEntry> collectLogsByTags(String... tags) {
        List<String> wantedTags = Arrays.asList(tags);
        List<LogEntry> logs = new ArrayList<>();
        for (LogEntry entry : memoryLogs) {
            if (wantedTags.contains(entry.tag)) {
                logs.add(entry);
            }
        }
        return logs;
    }

    private void clearLogsByTags(String... tags) {
        List<String> wantedTags = Arrays.asList(tags);
        List<LogEntry> toRemove = new ArrayList<>();
        for (LogEntry entry : memoryLogs) {
            if (wantedTags.contains(entry.tag)) {
                toRemove.add(entry);
            }
        }
        memoryLogs.removeAll(toRemove);
    }
}
