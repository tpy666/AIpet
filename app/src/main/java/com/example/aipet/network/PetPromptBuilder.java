package com.example.aipet.network;

import com.example.aipet.data.model.Pet;

/**
 * 宠物提示词生成器
 * 
 * 将宠物的各项特征（物种、性格、说话风格等）转换为系统提示词，
 * 用于指导 AI 模型生成符合宠物人设的回复。
 * 
 * 使用 PromptTemplate 集中管理所有提示词常量。
 */
public class PetPromptBuilder {
    
    /**
     * 根据宠物信息生成系统提示词
     * @param pet 宠物对象
     * @return 系统提示词
     */
    public static String buildSystemPrompt(Pet pet) {
        if (pet == null) {
            return PromptTemplate.getDefaultPrompt();
        }
        
        return PromptTemplate.buildFullPrompt(
            pet.getName() != null ? pet.getName() : "小宝宝",
            pet.getSpecies() != null ? pet.getSpecies() : "小伙伴",
            pet.getPersonality(),
            pet.getSpeakingStyle(),
            pet.getAppearance()
        );
    }
}
