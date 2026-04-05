package com.example.aipet.network.request;

import com.example.aipet.network.ChatRequest;
import com.example.aipet.network.DoubaoRequest;
import com.example.aipet.network.OpenAIChatRequest;

import java.util.List;
import java.util.Map;

/**
 * API 请求工厂类
 * 
 * 使用工厂模式统一创建各种类型的 API 请求对象，
 * 负责选择正确的请求类型并进行初始化
 */
public class ApiRequestFactory {
    
    /**
     * API 提供商类型枚举
     */
    public enum ProviderType {
        OPENAI("openai", OpenAIChatRequest.class),
        DOUBAO("doubao", DoubaoRequest.class),
        LOCAL("local", ChatRequest.class);
        
        private final String providerId;
        private final Class<?> requestClass;
        
        ProviderType(String providerId, Class<?> requestClass) {
            this.providerId = providerId;
            this.requestClass = requestClass;
        }
        
        public String getProviderId() {
            return providerId;
        }
        
        public Class<?> getRequestClass() {
            return requestClass;
        }
        
        public static ProviderType fromString(String provider) {
            for (ProviderType type : values()) {
                if (type.providerId.equalsIgnoreCase(provider)) {
                    return type;
                }
            }
            return LOCAL; // 默认使用本地类型
        }
    }
    
    /**
     * 创建 OpenAI 请求
     */
    public static BaseApiRequest createOpenAIRequest(String model, String userMessage) {
        return new OpenAIChatRequest(model, userMessage);
    }
    
    /**
     * 创建 OpenAI 请求（带系统提示）
     */
    public static BaseApiRequest createOpenAIRequest(String model, String systemPrompt, 
                                                     String userMessage, List<OpenAIChatRequest.Message> history) {
        OpenAIChatRequest request = new OpenAIChatRequest(model, systemPrompt, userMessage);
        if (history != null && !history.isEmpty()) {
            request.messages.clear();
            request.messages.addAll(history);
        }
        return request;
    }
    
    /**
     * 创建豆包请求
     */
    public static BaseApiRequest createDoubaoRequest(String model, String userMessage) {
        return new DoubaoRequest(model, userMessage);
    }
    
    /**
     * 创建豆包请求（带系统提示）
     */
    public static BaseApiRequest createDoubaoRequest(String model, String systemPrompt, String userMessage) {
        return new DoubaoRequest(model, userMessage, systemPrompt, true);
    }
    
    /**
     * 创建本地聊天请求
     */
    public static BaseApiRequest createChatRequest(String message, long petId) {
        return new ChatRequest(message, petId);
    }
    
    /**
     * 创建本地聊天请求（带宠物信息）
     */
    public static BaseApiRequest createChatRequest(String message, long petId, ChatRequest.PetInfo petInfo) {
        return new ChatRequest(message, petId, petInfo);
    }
    
    /**
     * 创建本地聊天请求（完整）
     */
    public static BaseApiRequest createChatRequest(String message, long petId, ChatRequest.PetInfo petInfo,
                                                   List<ChatRequest.Message> conversationHistory, 
                                                   Map<String, Object> options) {
        return new ChatRequest(message, petId, petInfo, conversationHistory, options);
    }
    
    /**
     * 通用工厂方法 - 根据提供商类型创建请求
     */
    public static BaseApiRequest createRequest(ProviderType provider, String model, 
                                               String userMessage) {
        switch (provider) {
            case OPENAI:
                return createOpenAIRequest(model, userMessage);
            case DOUBAO:
                return createDoubaoRequest(model, userMessage);
            case LOCAL:
            default:
                return createChatRequest(userMessage, 0); // petId=0 为默认值
        }
    }
    
    /**
     * 通用工厂方法 - 根据提供商类型创建请求（带系统提示）
     */
    public static BaseApiRequest createRequest(ProviderType provider, String model,
                                               String systemPrompt, String userMessage) {
        switch (provider) {
            case OPENAI:
                return createOpenAIRequest(model, systemPrompt, userMessage, null);
            case DOUBAO:
                return createDoubaoRequest(model, systemPrompt, userMessage);
            case LOCAL:
            default:
                ChatRequest request = new ChatRequest(userMessage, 0);
                request.systemPrompt = systemPrompt;
                return request;
        }
    }
}
