package com.example.aipet.network;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 字节跳动豆包 (ByteDance Doubao) API 响应体
 * 
 * API 文档: https://www.volcengine.com/docs/82379
 * 
 * 注：output 字段支持两种格式：
 *   1. 对象格式：{ "choices": [...] }
 *   2. 数组格式：[{ ... }]  （某些 API 端点返回）
 */
public class DoubaoResponse implements Serializable {

    /**
     * 请求 ID
     */
    @SerializedName("request_id")
    public String request_id;

    /**
     * 响应输出内容（支持对象或数组格式）
     */
    @SerializedName("output")
    public Output output;

    /**
     * 使用情况统计
     */
    @SerializedName("usage")
    public Usage usage;

    // ========== 便利方法 ==========

    /**
     * 获取 AI 的回复内容
     */
    public String getReplyContent() {
        if (output != null && output.choices != null && output.choices.length > 0) {
            Choice choice = output.choices[0];
            if (choice != null && choice.message != null && choice.message.content != null) {
                return choice.message.content;
            }
        }
        return "";
    }

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return output != null && output.choices != null && output.choices.length > 0;
    }

    @Override
    public String toString() {
        return "DoubaoResponse{" +
                "request_id='" + request_id + '\'' +
                ", replyContent='" + getReplyContent() + '\'' +
                '}';
    }

    // ========== 嵌套类 ==========

    /**
     * 响应输出 - 支持自动转换数组和对象格式
     */
    public static class Output implements Serializable {
        @SerializedName("choices")
        public Choice[] choices;

        @Override
        public String toString() {
            return "Output{" +
                    "choicesSize=" + (choices != null ? choices.length : 0) +
                    '}';
        }
    }

    /**
     * 自定义 JSON 反序列化器 - 支持 output 字段为对象或数组格式
     */
    public static class OutputDeserializer implements JsonDeserializer<Output> {
        @Override
        public Output deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            Output output = new Output();
            
            if (json == null || json.isJsonNull()) {
                return output;
            }
            
            try {
                if (json.isJsonObject()) {
                    // 标准格式：{ "choices": [...] }
                    JsonElement choicesElement = json.getAsJsonObject().get("choices");
                    if (choicesElement != null && choicesElement.isJsonArray()) {
                        output.choices = context.deserialize(choicesElement, Choice[].class);
                    } else {
                        output.choices = new Choice[0];
                    }
                } else if (json.isJsonArray()) {
                    // 数组格式：[{ "finish_reason": "...", "message": {...} }]
                    output.choices = context.deserialize(json, Choice[].class);
                } else {
                    output.choices = new Choice[0];
                }
            } catch (Exception e) {
                android.util.Log.e("DoubaoResponse", "Error deserializing output: " + e.getMessage(), e);
                output.choices = new Choice[0];
            }
            
            return output;
        }
    }

    /**
     * 选择项（可能有多个）
     */
    public static class Choice implements Serializable {
        @SerializedName("finish_reason")
        public String finish_reason;  // "stop" 等

        @SerializedName("message")
        public Message message;

        @Override
        public String toString() {
            return "Choice{" +
                    "finish_reason='" + finish_reason + '\'' +
                    ", message=" + message +
                    '}';
        }
    }

    /**
     * 消息内容
     */
    public static class Message implements Serializable {
        @SerializedName("role")
        public String role;  // "assistant"

        @SerializedName("content")
        public String content;

        @Override
        public String toString() {
            return "Message{" +
                    "role='" + role + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }

    /**
     * 使用情况统计
     */
    public static class Usage implements Serializable {
        /**
         * 输入 token 数
         */
        @SerializedName("input_tokens")
        public int input_tokens;

        /**
         * 输出 token 数
         */
        @SerializedName("output_tokens")
        public int output_tokens;

        /**
         * 总 token 数
         */
        public int getTotalTokens() {
            return input_tokens + output_tokens;
        }

        @Override
        public String toString() {
            return "Usage{" +
                    "input_tokens=" + input_tokens +
                    ", output_tokens=" + output_tokens +
                    ", total=" + getTotalTokens() +
                    '}';
        }
    }
}
