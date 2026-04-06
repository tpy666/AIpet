package com.example.aipet.data.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 聊天消息数据模型
 * 用于存储用户与 AI 宠物之间的对话消息
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    // 消息角色常量
    public static final String ROLE_USER = "user";        // 用户消息
    public static final String ROLE_ASSISTANT = "assistant"; // AI 助手消息

    // 属性字段
    private long id;                    // 消息 ID（数据库主键）
    private String role;                // 消息角色：user 或 assistant
    private String content;             // 消息内容
    private long timestamp;             // 时间戳（毫秒）
    private long petId;                 // 关联的宠物 ID（可选）

    /**
     * 无参构造方法
     */
    public Message() {
    }

    /**
     * 最小参数构造方法（创建新消息）
     */
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 带宠物 ID 的构造方法
     */
    public Message(String role, String content, long petId) {
        this.role = role;
        this.content = content;
        this.petId = petId;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 完整参数构造方法
     */
    public Message(long id, String role, String content, long timestamp, long petId) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.petId = petId;
    }

    /**
     * 不含 ID 的完整参数构造方法
     */
    public Message(String role, String content, long timestamp, long petId) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.petId = petId;
    }

    // ==================== Getter 方法 ====================

    public long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getPetId() {
        return petId;
    }

    /**
     * 判断消息是否来自用户
     */
    public boolean isFromUser() {
        return ROLE_USER.equals(role);
    }

    /**
     * 判断消息是否来自 AI 助手
     */
    public boolean isFromAssistant() {
        return ROLE_ASSISTANT.equals(role);
    }

    // ==================== Setter 方法 ====================

    public void setId(long id) {
        this.id = id;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPetId(long petId) {
        this.petId = petId;
    }

    // ==================== 辅助方法 ====================

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", role='" + role + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", petId=" + petId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message message = (Message) o;
        return id == message.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
