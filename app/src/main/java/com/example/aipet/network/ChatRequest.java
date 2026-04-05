package com.example.aipet.network;

import com.example.aipet.network.request.BaseApiRequest;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 增强的聊天请求体类
 * 
 * 包含用户消息、宠物上下文、对话历史等信息，
 * 使 AI 能够根据宠物的个性特征和对话历史生成更personalized的回复。
 */
public class ChatRequest extends BaseApiRequest {
    
    @SerializedName("message")
    public String message;
    
    @SerializedName("petId")
    public long petId;
    
    // ========== 宠物上下文 ==========
    
    @SerializedName("petInfo")
    public PetInfo petInfo;
    
    // ========== 系统提示词 ==========
    
    @SerializedName("systemPrompt")
    public String systemPrompt;
    
    // ========== 对话历史 ==========
    
    @SerializedName("conversationHistory")
    public List<Message> conversationHistory;
    
    // ========== 其他选项 ==========
    
    @SerializedName("options")
    public Map<String, Object> options;
    
    // ========== 构造方法 ==========
    
    /**
     * 最简单的构造 - 仅消息和宠物 ID
     */
    public ChatRequest(String message, long petId) {
        this.message = message;
        this.petId = petId;
        this.conversationHistory = new ArrayList<>();
        this.options = new HashMap<>();
    }
    
    /**
     * 包含宠物信息的构造
     */
    public ChatRequest(String message, long petId, PetInfo petInfo) {
        this.message = message;
        this.petId = petId;
        this.petInfo = petInfo;
        this.conversationHistory = new ArrayList<>();
        this.options = new HashMap<>();
    }
    
    /**
     * 完整构造 - 包含所有信息
     */
    public ChatRequest(String message, long petId, PetInfo petInfo, 
                      List<Message> conversationHistory, Map<String, Object> options) {
        this.message = message;
        this.petId = petId;
        this.petInfo = petInfo;
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
        this.options = options != null ? options : new HashMap<>();
    }

    @Override
    public String getModel() {
        // 本地请求没有具体的模型概念，返回 "local"
        return "local";
    }
    
    @Override
    public String getRequestType() {
        return "Local";
    }
    
    @Override
    public String getUserMessage() {
        return message;
    }
    
    // ========== 便利方法 ==========
    
    /**
     * 添加对话历史
     */
    public void addToHistory(String role, String content) {
        if (conversationHistory == null) {
            conversationHistory = new ArrayList<>();
        }
        Message msg = new Message();
        msg.setRole(role);
        msg.setContent(content);
        conversationHistory.add(msg);
    }
    
    /**
     * 设置请求选项
     */
    public void setOption(String key, Object value) {
        if (options == null) {
            options = new HashMap<>();
        }
        options.put(key, value);
    }
    
    /**
     * 获取请求选项
     */
    public Object getOption(String key) {
        if (options == null) {
            return null;
        }
        return options.get(key);
    }
    
    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", petId=" + petId +
                ", petInfo=" + petInfo +
                ", historySize=" + (conversationHistory != null ? conversationHistory.size() : 0) +
                '}';
    }
    
    // ========== 嵌套类：宠物信息 ==========
    
    /**
     * 宠物信息结构 - 发送给 API 的上下文信息
     */
    public static class PetInfo implements Serializable {
        
        @SerializedName("id")
        public long id;
        
        @SerializedName("name")
        public String name;
        
        @SerializedName("species")
        public String species;  // 物种：猫、狗、兔子、狐狸、龙、小熊
        
        @SerializedName("personality")
        public String personality;  // 性格：温柔、活泼、高冷、撒娇、稳重
        
        @SerializedName("speakingStyle")
        public String speakingStyle;  // 说话风格：卖萌、暖心、幽默、文艺、直白
        
        @SerializedName("appearance")
        public String appearance;  // 外观描述
        
        @SerializedName("avatar")
        public String avatar;  // 头像 URL
        
        // ========== 构造方法 ==========
        
        public PetInfo() {}
        
        /**
         * 从 Pet 对象转换
         */
        public PetInfo(com.example.aipet.data.model.Pet pet) {
            if (pet != null) {
                this.id = pet.getId();
                this.name = pet.getName();
                this.species = pet.getSpecies();
                this.personality = pet.getPersonality();
                this.speakingStyle = pet.getSpeakingStyle();
                this.appearance = pet.getAppearance();
                this.avatar = pet.getAvatar();
            }
        }
        
        public PetInfo(long id, String name, String species, String personality,
                      String speakingStyle, String appearance, String avatar) {
            this.id = id;
            this.name = name;
            this.species = species;
            this.personality = personality;
            this.speakingStyle = speakingStyle;
            this.appearance = appearance;
            this.avatar = avatar;
        }
        
        // ========== 便利方法 ==========
        
        /**
         * 获取宠物简要描述
         */
        public String getDescription() {
            return String.format(
                "宠物：%s（%s）| 性格：%s | 说话风格：%s | 外观：%s",
                name, species, personality, speakingStyle, appearance
            );
        }
        
        /**
         * 构建宠物系统提示词
         * 用于 OpenAI API 的 system role
         */
        public String getSystemPrompt() {
            return String.format(
                "你是一个名叫 %s 的 %s 虚拟宠物助手。\n" +
                "你的性格特征：%s\n" +
                "你的说话风格：%s\n" +
                "你的外观：%s\n" +
                "请用你独特的个性来回复用户的消息。",
                name, species, personality, speakingStyle, appearance
            );
        }
        
        @Override
        public String toString() {
            return "PetInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", species='" + species + '\'' +
                    ", personality='" + personality + '\'' +
                    ", speakingStyle='" + speakingStyle + '\'' +
                    '}';
        }
    }
    
    // ========== 嵌套类：简单消息 ==========
    
    /**
     * 对话历史中的消息结构（简化版）
     */
    public static class Message implements Serializable {
        
        @SerializedName("role")
        public String role;  // "user" 或 "assistant"
        
        @SerializedName("content")
        public String content;
        
        public Message() {}
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "Message{" +
                    "role='" + role + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
}
