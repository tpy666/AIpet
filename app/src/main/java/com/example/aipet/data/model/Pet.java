package com.example.aipet.data.model;

import java.io.Serializable;

/**
 * 宠物角色数据模型
 * 用于存储 AI 宠物的基本信息
 */
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    // 属性字段
    private long id;                    // 宠物 ID（数据库主键）
    private String name;                // 宠物名称
    private String species;             // 物种（如：猫、狗、兔子等）
    private String personality;         // 性格描述（如：调皮、温和、高冷等）
    private String speakingStyle;       // 说话风格（如：卖萌、严肃、幽默等）
    private String appearance;          // 外观描述（如：黄色、长毛、蓝眼睛等）
    private String avatar;              // 头像 URL 或本地路径

    /**
     * 无参构造方法
     */
    public Pet() {
    }

    /**
     * 带所有参数的构造方法
     */
    public Pet(long id, String name, String species, String personality,
               String speakingStyle, String appearance, String avatar) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.personality = personality;
        this.speakingStyle = speakingStyle;
        this.appearance = appearance;
        this.avatar = avatar;
    }

    /**
     * 不含 ID 的构造方法（用于新建宠物）
     */
    public Pet(String name, String species, String personality,
               String speakingStyle, String appearance, String avatar) {
        this.name = name;
        this.species = species;
        this.personality = personality;
        this.speakingStyle = speakingStyle;
        this.appearance = appearance;
        this.avatar = avatar;
    }

    // ==================== Getter 方法 ====================

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public String getPersonality() {
        return personality;
    }

    public String getSpeakingStyle() {
        return speakingStyle;
    }

    public String getAppearance() {
        return appearance;
    }

    public String getAvatar() {
        return avatar;
    }

    // ==================== Setter 方法 ====================

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public void setSpeakingStyle(String speakingStyle) {
        this.speakingStyle = speakingStyle;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    // ==================== 辅助方法 ====================

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", species='" + species + '\'' +
                ", personality='" + personality + '\'' +
                ", speakingStyle='" + speakingStyle + '\'' +
                ", appearance='" + appearance + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pet pet = (Pet) o;
        return id == pet.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
