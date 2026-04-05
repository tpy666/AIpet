package com.example.aipet.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * API 聊天响应体类
 * 
 * 支持多种 API 响应格式：
 * • 简单格式：仅包含 reply
 * • OpenAI 格式：包含 choices
 * • 复杂格式：包含完整的元数据和资源使用情况
 */
public class ChatResponse implements Serializable {
    
    /**
     * 简单响应：直接回复
     */
    @SerializedName("reply")
    public String reply;
    
    /**
     * OpenAI 格式：选择列表
     */
    @SerializedName("choices")
    public Choice[] choices;
    
    /**
     * 响应状态码
     */
    @SerializedName("code")
    public int code;
    
    /**
     * 响应消息
     */
    @SerializedName("message")
    public String message;
    
    /**
     * 用于 OpenAI API 的使用统计
     */
    @SerializedName("usage")
    public Usage usage;
    
    /**
     * 请求 ID（用于追踪）
     */
    @SerializedName("id")
    public String id;
    
    /**
     * 模型名称（来自 OpenAI）
     */
    @SerializedName("model")
    public String model;
    
    // ========== 便利方法 ==========
    
    /**
     * 获取最终的回复内容
     * 
     * 支持多种格式：
     * 1. 直接 reply 字段
     * 2. OpenAI choices[0].message.content
     * 3. OpenAI choices[0].text
     */
    public String getReplyContent() {
        // 优先尝试直接 reply 字段
        if (reply != null && !reply.isEmpty()) {
            return reply;
        }
        
        // 尝试 OpenAI choices 格式
        if (choices != null && choices.length > 0) {
            Choice choice = choices[0];
            
            // 尝试 message.content（新格式）
            if (choice.message != null && choice.message.content != null) {
                return choice.message.content;
            }
            
            // 尝试 text（旧格式）
            if (choice.text != null) {
                return choice.text;
            }
        }
        
        return "";
    }
    
    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        // 检查 HTTP 状态码标记
        if (code > 0) {
            return code >= 200 && code < 300;
        }
        
        // 如果有 reply 或 choices，认为成功
        return getReplyContent() != null && !getReplyContent().isEmpty();
    }
    
    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        if (message != null && !message.isEmpty()) {
            return message;
        }
        if (code > 0 && !isSuccess()) {
            return "HTTP " + code;
        }
        return "未知错误";
    }
    
    /**
     * 获取使用的 token 数量
     */
    public int getTotalTokens() {
        if (usage != null) {
            return usage.total_tokens;
        }
        return -1;
    }
    
    /**
     * 获取输入 token 数量
     */
    public int getPromptTokens() {
        if (usage != null) {
            return usage.prompt_tokens;
        }
        return -1;
    }
    
    /**
     * 获取输出 token 数量
     */
    public int getCompletionTokens() {
        if (usage != null) {
            return usage.completion_tokens;
        }
        return -1;
    }
    
    @Override
    public String toString() {
        return "ChatResponse{" +
                "reply='" + (reply != null ? reply.substring(0, Math.min(50, reply.length())) : "null") + "...'" +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", hasChoices=" + (choices != null && choices.length > 0) +
                '}';
    }
    
    // ========== 嵌套类：Choice（OpenAI 格式） ==========
    
    /**
     * OpenAI 响应的选择项
     */
    public static class Choice implements Serializable {
        
        @SerializedName("message")
        public MessageContent message;
        
        @SerializedName("text")
        public String text;
        
        @SerializedName("index")
        public int index;
        
        @SerializedName("finish_reason")
        public String finishReason;
        
        @Override
        public String toString() {
            return "Choice{" +
                    "finishReason='" + finishReason + '\'' +
                    '}';
        }
    }
    
    /**
     * OpenAI 消息内容（新格式）
     */
    public static class MessageContent implements Serializable {
        
        @SerializedName("role")
        public String role;
        
        @SerializedName("content")
        public String content;
        
        @Override
        public String toString() {
            return "MessageContent{" +
                    "role='" + role + '\'' +
                    ", content='" + (content != null ? content.substring(0, Math.min(30, content.length())) : "null") + "...'" +
                    '}';
        }
    }
    
    /**
     * Token 使用统计（OpenAI 格式）
     */
    public static class Usage implements Serializable {
        
        @SerializedName("prompt_tokens")
        public int prompt_tokens;
        
        @SerializedName("completion_tokens")
        public int completion_tokens;
        
        @SerializedName("total_tokens")
        public int total_tokens;
        
        @Override
        public String toString() {
            return "Usage{" +
                    "prompt=" + prompt_tokens +
                    ", completion=" + completion_tokens +
                    ", total=" + total_tokens +
                    '}';
        }
    }
}
