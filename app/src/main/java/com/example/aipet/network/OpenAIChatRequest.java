package com.example.aipet.network;

import com.example.aipet.network.request.BaseApiRequest;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI API 标准格式的聊天请求
 * 
 * 用于与 OpenAI ChatGPT API 通信
 * API 文档: https://platform.openai.com/docs/api-reference/chat/create
 */
public class OpenAIChatRequest extends BaseApiRequest {
    
    /**
     * 所使用的模型 ID (如 gpt-3.5-turbo、gpt-4 等)
     */
    @SerializedName("model")
    public String model;
    
    /**
     * 消息列表 - 包含对话历史
     */
    @SerializedName("messages")
    public List<Message> messages;
    
    /**
     * 采样温度，控制随机性 (0-2)
     * 值越低，结果越确定性；值越高，结果越随机
     */
    @SerializedName("temperature")
    public float temperature = 0.7f;
    
    /**
     * 最大生成的 Token 数
     */
    @SerializedName("max_tokens")
    public int maxTokens = 500;
    
    /**
     * Top P 采样参数
     */
    @SerializedName("top_p")
    public float topP = 1.0f;
    
    /**
     * 频率惩罚参数
     */
    @SerializedName("frequency_penalty")
    public float frequencyPenalty = 0.0f;
    
    /**
     * 存在惩罚参数
     */
    @SerializedName("presence_penalty")
    public float presencePenalty = 0.0f;
    
    /**
     * 构造函数 - 创建单个消息的请求
     */
    public OpenAIChatRequest(String model, String userMessage) {
        this.model = model;
        this.messages = new ArrayList<>();
        
        // 添加系统提示（可选）
        Message systemMsg = new Message();
        systemMsg.role = "system";
        systemMsg.content = "你是一个友好的虚拟宠物助手。";
        this.messages.add(systemMsg);
        
        // 添加用户消息
        Message userMsg = new Message();
        userMsg.role = "user";
        userMsg.content = userMessage;
        this.messages.add(userMsg);
    }

    /**
     * 构造函数 - 创建带系统提示词的请求
     */
    public OpenAIChatRequest(String model, String systemPrompt, String userMessage) {
        this.model = model;
        this.messages = new ArrayList<>();
        
        // 添加系统提示
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Message systemMsg = new Message();
            systemMsg.role = "system";
            systemMsg.content = systemPrompt;
            this.messages.add(systemMsg);
        } else {
            // 默认系统提示
            Message systemMsg = new Message();
            systemMsg.role = "system";
            systemMsg.content = "你是一个友好的虚拟宠物助手。";
            this.messages.add(systemMsg);
        }
        
        // 添加用户消息
        Message userMsg = new Message();
        userMsg.role = "user";
        userMsg.content = userMessage;
        this.messages.add(userMsg);
    }
    
    /**
     * 构造函数 - 创建带对话历史的请求
     */
    public OpenAIChatRequest(String model, List<Message> conversationHistory) {
        this.model = model;
        this.messages = conversationHistory != null ? new ArrayList<>(conversationHistory) : new ArrayList<>();
        
        // 如果消息列表不为空，确保最后一条是用户消息
        if (this.messages.isEmpty()) {
            Message systemMsg = new Message();
            systemMsg.role = "system";
            systemMsg.content = "你是一个友好的虚拟宠物助手。";
            this.messages.add(systemMsg);
        }
    }
    
    @Override
    public String getModel() {
        return model;
    }
    
    @Override
    public String getRequestType() {
        return "OpenAI";
    }
    
    @Override
    public String getUserMessage() {
        // 获取最后一条用户消息
        if (messages != null && !messages.isEmpty()) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                if ("user".equals(messages.get(i).role)) {
                    return messages.get(i).content;
                }
            }
        }
        return "";
    }
    
    /**
     * OpenAI API 消息格式
     */
    public static class Message implements Serializable {
        /**
         * 消息角色: "system"、"user"、"assistant"
         */
        @SerializedName("role")
        public String role;
        
        /**
         * 消息内容
         */
        @SerializedName("content")
        public String content;
        
        /**
         * 消息名称（可选）
         */
        @SerializedName("name")
        public String name;
        
        public Message() {}
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        @Override
        public String toString() {
            return role + ": " + (content != null ? content.substring(0, Math.min(30, content.length())) : "null");
        }
    }
    
    @Override
    public String toString() {
        return "OpenAIChatRequest{" +
                "model='" + model + '\'' +
                ", messageCount=" + (messages != null ? messages.size() : 0) +
                ", temperature=" + temperature +
                '}';
    }
}
