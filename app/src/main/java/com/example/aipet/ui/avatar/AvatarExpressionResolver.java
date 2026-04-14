package com.example.aipet.ui.avatar;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * 根据角色回复文本推断情绪，并生成差分图资源路径。
 */
public final class AvatarExpressionResolver {

    public static final String EMOTION_NORMAL = "normal";

    private AvatarExpressionResolver() {
    }

    @NonNull
    public static String resolveEmotionTag(@Nullable String replyText) {
        if (TextUtils.isEmpty(replyText)) {
            return EMOTION_NORMAL;
        }

        String text = replyText.toLowerCase(Locale.ROOT);

        if (containsAny(text, "生气", "愤怒", "烦", "火大", "angry", "mad")) {
            return "angry";
        }
        if (containsAny(text, "难过", "伤心", "沮丧", "失落", "sad", "upset")) {
            return "sad";
        }
        if (containsAny(text, "害羞", "不好意思", "脸红", "shy")) {
            return "shy";
        }
        if (containsAny(text, "惊", "震惊", "意外", "surprised", "wow")) {
            return "surprised";
        }
        if (containsAny(text, "兴奋", "激动", "冲呀", "excited", "let's go")) {
            return "excited";
        }
        if (containsAny(text, "开心", "高兴", "喜欢", "笑", "happy", "glad", "great")) {
            return "happy";
        }
        return EMOTION_NORMAL;
    }

    @Nullable
    public static String buildDiffAssetSource(@Nullable String outfitId, @NonNull String emotionTag) {
        if (TextUtils.isEmpty(outfitId)) {
            return null;
        }
        String normalizedEmotion = TextUtils.isEmpty(emotionTag) ? EMOTION_NORMAL : emotionTag;
        String fileName = String.format(Locale.ROOT, "%s__exp_%s__v001.png", outfitId, normalizedEmotion);
        return "asset:character/diff/" + outfitId + "/" + normalizedEmotion + "/" + fileName;
    }

    @Nullable
    public static String buildDiffAssetSourceFromReply(@Nullable String outfitId, @Nullable String replyText) {
        String emotionTag = resolveEmotionTag(replyText);
        return buildDiffAssetSource(outfitId, emotionTag);
    }

    private static boolean containsAny(@NonNull String text, @NonNull String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
