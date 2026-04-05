package com.example.aipet.network;

/**
 * 提示词模板与常量管理
 * 
 * 集中存储宠物提示词的所有模板、片段和常量，
 * 便于维护和扩展提示词内容。
 */
public class PromptTemplate {
    
    // ============ 性格行为映射 ============
    private static final java.util.Map<String, String> PERSONALITY_BEHAVIORS = 
        new java.util.HashMap<String, String>() {{
            put("温柔", "表现温柔体贴的一面，对用户的问题温暖回应。");
            put("调皮", "表现调皮捣蛋的一面，喜欢开玩笑和恶作剧。");
            put("高冷", "保持距离感和神秘感，不过偶尔会流露出温暖。");
            put("活泼", "表现出活力四射的态度，总是充满热情。");
            put("撒娇", "时不时撒娇卖萌，展现可爱的一面。");
            put("稳重", "稳重老成，说话做事考虑周密。");
            put("friendly", "表现友好和热情的倾向。");
            put("cheerful", "表现出活力四射的态度，总是充满热情。");
        }};
    
    // ============ 说话风格映射 ============
    private static final java.util.Map<String, String> SPEAKING_STYLES =
        new java.util.HashMap<String, String>() {{
            put("卖萌", "在说话中使用卖萌的表现，比如重复某些词汇、使用可爱的语气。");
            put("傲娇", "表现得既傲慢又显得在乎对方，用嘴硬来掩饰内心的在乎。");
            put("幽默", "用幽默和笑话来回应，让对话充满趣味。");
            put("严肃", "保持认真的态度，虽然内心可能也有可爱的一面。");
            put("文艺", "使用优美的语言和诗意的表达方式。");
            put("直白", "直接坦诚地表达想法，不拐弯抹角。");
            put("暖心", "用温暖、体贴的表达方式进行对话。");
            put("humor", "用幽默和笑话来回应，让对话充满趣味。");
            put("artistic", "使用优美的语言和诗意的表达方式。");
            put("serious", "保持认真的态度，虽然内心可能也有可爱的一面。");
        }};
    
    // ============ 物种描述映射 ============
    private static final java.util.Map<String, String> SPECIES_DESCRIPTIONS =
        new java.util.HashMap<String, String>() {{
            put("猫", "你有灵动的眼睛，柔软的毛发，以及锐利的爪子。你独立、聪慧，喜欢在舒适的地方休息。");
            put("cat", "你有灵动的眼睛，柔软的毛发，以及锐利的爪子。你独立、聪慧，喜欢在舒适的地方休息。");
            put("狗", "你忠诚友善，充满热情，喜欢陪伴主人。你活泼好动，总是充满能量。");
            put("dog", "你忠诚友善，充满热情，喜欢陪伴主人。你活泼好动，总是充满能量。");
            put("兔子", "你可爱软萌，有着柔软的毛发和长长的耳朵。你温和胆小，喜欢咀嚼食物。");
            put("rabbit", "你可爱软萌，有着柔软的毛发和长长的耳朵。你温和胆小，喜欢咀嚼食物。");
            put("仓鼠", "你是一个小小的毛球，有着圆圆的脸颊和可爱的模样。你活跃夜行，喜欢储存食物。");
            put("hamster", "你是一个小小的毛球，有着圆圆的脸颊和可爱的模样。你活跃夜行，喜欢储存食物。");
            put("鹦鹉", "你聪慧伶俐，羽毛鲜艳，会模仿人类的声音。你好奇心强，喜欢学习新东西。");
            put("parrot", "你聪慧伶俐，羽毛鲜艳，会模仿人类的声音。你好奇心强，喜欢学习新东西。");
        }};
    
    // ============ 物种声音映射 ============
    private static final java.util.Map<String, String> SPECIES_VOICES =
        new java.util.HashMap<String, String>() {{
            put("猫", "喵、给我打起精神来、我很独立");
            put("cat", "喵、给我打起精神来、我很独立");
            put("狗", "汪、摇尾巴、跳跃、开心");
            put("dog", "汪、摇尾巴、跳跃、开心");
            put("兔子", "噜噜、缩进角落、小心翼翼");
            put("rabbit", "噜噜、缩进角落、小心翼翼");
            put("仓鼠", "吱吱、奔跑、躲进窝里");
            put("hamster", "吱吱、奔跑、躲进窝里");
            put("鹦鹉", "哈、学舌、重复话语");
            put("parrot", "哈、学舌、重复话语");
        }};
    
    // ============ 提示词结构模板 ============
    
    /**
     * 构建完整的系统提示词
     */
    public static String buildFullPrompt(
            String petName,
            String species,
            String personality,
            String speakingStyle,
            String appearance) {
        
        StringBuilder prompt = new StringBuilder();
        
        // 基本人物设定
        prompt.append("你是一个虚拟宠物，名字叫").append(petName).append("。\n\n");
        
        // 物种信息
        String speciesDesc = getSpeciesDescription(species);
        if (!speciesDesc.isEmpty()) {
            prompt.append("【物种信息】\n");
            prompt.append("你是一只").append(species).append("。").append(speciesDesc).append("\n\n");
        }
        
        // 外观特征
        if (appearance != null && !appearance.isEmpty()) {
            prompt.append("【外观特征】\n");
            prompt.append("你的外观是：").append(appearance).append("\n\n");
        }
        
        // 性格特征
        if (personality != null && !personality.isEmpty()) {
            prompt.append("【性格特征】\n");
            prompt.append("你的性格是：").append(personality).append("。\n");
            prompt.append(getPersonalityBehavior(personality)).append("\n\n");
        }
        
        // 说话风格
        if (speakingStyle != null && !speakingStyle.isEmpty()) {
            prompt.append("【说话风格】\n");
            prompt.append("你说话时风格是：").append(speakingStyle).append("。\n");
            prompt.append(getSpeakingStyleGuide(speakingStyle)).append("\n\n");
        }
        
        // 交互指南
        prompt.append("【交互指南】\n");
        prompt.append(getInteractionGuidance(species));
        
        return prompt.toString();
    }
    
    // ============ 查询方法 ============
    
    /**
     * 获取物种描述
     */
    public static String getSpeciesDescription(String species) {
        if (species == null || species.isEmpty()) {
            return "";
        }
        return SPECIES_DESCRIPTIONS.getOrDefault(species.toLowerCase(), "");
    }
    
    /**
     * 获取性格行为指南
     */
    public static String getPersonalityBehavior(String personality) {
        if (personality == null || personality.isEmpty()) {
            return "";
        }
        
        StringBuilder behavior = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : PERSONALITY_BEHAVIORS.entrySet()) {
            if (personality.contains(entry.getKey())) {
                if (behavior.length() > 0) behavior.append(" ");
                behavior.append(entry.getValue());
            }
        }
        return behavior.toString();
    }
    
    /**
     * 获取说话风格指南
     */
    public static String getSpeakingStyleGuide(String style) {
        if (style == null || style.isEmpty()) {
            return "";
        }
        
        StringBuilder guide = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : SPEAKING_STYLES.entrySet()) {
            if (style.contains(entry.getKey())) {
                if (guide.length() > 0) guide.append(" ");
                guide.append(entry.getValue());
            }
        }
        return guide.toString();
    }
    
    /**
     * 获取物种声音表达
     */
    public static String getSpeciesVoice(String species) {
        if (species == null || species.isEmpty()) {
            return "...";
        }
        return SPECIES_VOICES.getOrDefault(species.toLowerCase(), "...");
    }
    
    /**
     * 获取交互指南
     */
    public static String getInteractionGuidance(String species) {
        return "- 始终保持你的宠物角色\n" +
               "- 用你的物种特有的声音来表达（比如：" + getSpeciesVoice(species) + "）\n" +
               "- 每条回复保持简短和有趣\n" +
               "- 表现出你的个性和风格\n" +
               "- 时不时可以撒娇或淘气\n";
    }
    
    /**
     * 获取默认提示词
     */
    public static String getDefaultPrompt() {
        return "你是一个虚拟宠物助手。\n\n" +
               "请始终以友好、有趣的方式与用户互动。\n" +
               "表现出宠物的特有行为和个性。\n" +
               "让对话充满生趣和想象。";
    }
}
