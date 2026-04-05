package com.example.aipet.network;

import androidx.annotation.Nullable;
import com.example.aipet.network.request.BaseApiRequest;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 字节跳动豆包 (ByteDance Doubao) API 请求体
 * 
 * 支持多模态输入：文本 + 图片
 * 
 * API 文档: https://www.volcengine.com/docs/82379
 * Endpoint: https://ark.cn-beijing.volces.com/api/v3/responses
 */
public class DoubaoRequest extends BaseApiRequest {

    /**
     * 模型 ID，例如: doubao-seed-2-0-lite-260215
     */
    @SerializedName("model")
    public String model;

    /**
     * 输入数据 - 包含用户消息和可选的图片
     */
    @SerializedName("input")
    public List<InputMessage> input;

    /**
     * 生成参数（可选）
     */
    @SerializedName("parameters")
    public Parameters parameters;

    // ========== 构造方法 ==========

    public DoubaoRequest(String model, String textMessage) {
        this.model = model;
        this.input = new ArrayList<>();
        
        InputMessage msg = new InputMessage();
        msg.role = "user";
        msg.content = new ArrayList<>();
        
        // 添加文本
        TextContent textContent = new TextContent();
        textContent.type = "input_text";
        textContent.text = textMessage;
        msg.content.add(textContent);
        
        this.input.add(msg);
    }

    /**
     * 包含系统提示词的构造方法
     * 参数顺序: 模型、消息、系统提示（以区分 (String, String, String, String) 的图片构造）
     */
    public DoubaoRequest(String model, String textMessage, @Nullable String systemPrompt, boolean hasSystemPrompt) {
        this.model = model;
        this.input = new ArrayList<>();
        
        // 添加系统提示
        if (systemPrompt != null && !systemPrompt.isEmpty() && hasSystemPrompt) {
            InputMessage systemMsg = new InputMessage();
            systemMsg.role = "system";
            systemMsg.content = new ArrayList<>();
            
            TextContent systemContent = new TextContent();
            systemContent.type = "input_text";
            systemContent.text = systemPrompt;
            systemMsg.content.add(systemContent);
            
            this.input.add(systemMsg);
        }
        
        // 添加用户消息
        InputMessage userMsg = new InputMessage();
        userMsg.role = "user";
        userMsg.content = new ArrayList<>();
        
        TextContent textContent = new TextContent();
        textContent.type = "input_text";
        textContent.text = textMessage;
        userMsg.content.add(textContent);
        
        this.input.add(userMsg);
    }

    /**
     * 静态工厂方法 - 创建带系统提示的请求
     */
    public static DoubaoRequest withSystemPrompt(String model, String systemPrompt, String textMessage) {
        return new DoubaoRequest(model, textMessage, systemPrompt, true);
    }

    public DoubaoRequest(String model, String textMessage, String imageUrl) {
        this.model = model;
        this.input = new ArrayList<>();
        
        InputMessage msg = new InputMessage();
        msg.role = "user";
        msg.content = new ArrayList<>();
        
        // 添加图片
        ImageContent imageContent = new ImageContent();
        imageContent.type = "input_image";
        imageContent.image_url = imageUrl;
        msg.content.add(imageContent);
        
        // 添加文本
        TextContent textContent = new TextContent();
        textContent.type = "input_text";
        textContent.text = textMessage;
        msg.content.add(textContent);
        
        this.input.add(msg);
    }

    // ========== 嵌套类 ==========

    /**
     * 输入消息结构
     */
    public static class InputMessage implements Serializable {
        @SerializedName("role")
        public String role;  // "user" 或 "assistant"

        @SerializedName("content")
        public List<ContentItem> content;

        @Override
        public String toString() {
            return "InputMessage{" +
                    "role='" + role + '\'' +
                    ", contentSize=" + (content != null ? content.size() : 0) +
                    '}';
        }
    }

    /**
     * 内容项接口 - 可以是文本或图片
     */
    public static class ContentItem implements Serializable {
        @SerializedName("type")
        public String type;  // "input_text" 或 "input_image"
    }

    /**
     * 文本内容
     */
    public static class TextContent extends ContentItem implements Serializable {
        @SerializedName("text")
        public String text;

        public TextContent() {
            this.type = "input_text";
        }
    }

    /**
     * 图片内容
     */
    public static class ImageContent extends ContentItem implements Serializable {
        @SerializedName("image_url")
        public String image_url;

        public ImageContent() {
            this.type = "input_image";
        }
    }

    @Override
    public String getModel() {
        return model;
    }
    
    @Override
    public String getRequestType() {
        return "Doubao";
    }
    
    @Override
    public String getUserMessage() {
        // 获取最后一条用户消息的文本内容
        if (input != null && !input.isEmpty()) {
            for (int i = input.size() - 1; i >= 0; i--) {
                InputMessage msg = input.get(i);
                if ("user".equals(msg.role) && msg.content != null && !msg.content.isEmpty()) {
                    for (ContentItem content : msg.content) {
                        if (content instanceof TextContent) {
                            return ((TextContent) content).text;
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * 生成参数
     */
    public static class Parameters implements Serializable {
        /**
         * 最大生成 token 数
         */
        @SerializedName("max_new_tokens")
        public int max_new_tokens = 1024;

        /**
         * 温度（控制生成的创意性）
         */
        @SerializedName("temperature")
        public double temperature = 0.7;

        /**
         * Top-P 采样
         */
        @SerializedName("top_p")
        public double top_p = 0.9;

        /**
         * 是否流式输出
         */
        @SerializedName("use_stream_chat_acl")
        public boolean use_stream_chat_acl = false;
    }

    @Override
    public String toString() {
        return "DoubaoRequest{" +
                "model='" + model + '\'' +
                ", inputSize=" + (input != null ? input.size() : 0) +
                '}';
    }
}
