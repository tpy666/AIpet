package com.example.aipet.network;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 回复格式化器。
 *
 * 作用：
 * 1) 区分深度思考与最终回答
 * 2) 清理 SDK toString 调用痕迹与代码调用片段
 */
public final class AssistantReplyFormatter {

    private static final Pattern OUTPUT_TEXT_PATTERN = Pattern.compile("output_text[^)]*?text='(.*?)'", Pattern.DOTALL);
    private static final Pattern SUMMARY_TEXT_PATTERN = Pattern.compile("summary[^\\]]*?text='(.*?)'", Pattern.DOTALL);
    private static final Pattern REASONING_TEXT_PATTERN = Pattern.compile("reasoning[^\\]]*?text='(.*?)'", Pattern.DOTALL);
    private static final Pattern GENERIC_TEXT_PATTERN = Pattern.compile("text='(.*?)'", Pattern.DOTALL);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```", Pattern.DOTALL);

    private AssistantReplyFormatter() {
    }

    @NonNull
    public static FormatResult format(@NonNull String rawReply) {
        String raw = rawReply.trim();
        if (raw.isEmpty()) {
            return new FormatResult("", "", "");
        }

        String thinking = joinCaptured(SUMMARY_TEXT_PATTERN, raw);
        if (thinking.isEmpty()) {
            thinking = joinCaptured(REASONING_TEXT_PATTERN, raw);
        }

        String answer = joinCaptured(OUTPUT_TEXT_PATTERN, raw);
        if (answer.isEmpty()) {
            answer = extractBestAnswer(raw);
        }

        thinking = cleanupText(thinking);
        answer = cleanupText(answer);

        // 常规回复（非 SDK 调用痕迹）兜底
        if (answer.isEmpty() && !looksLikeSdkTrace(raw)) {
            answer = cleanupText(raw);
        }

        String displayText;
        if (!thinking.isEmpty() && !answer.isEmpty()) {
            displayText = "【深度思考】\n" + thinking + "\n\n【回答】\n" + answer;
        } else if (!answer.isEmpty()) {
            displayText = answer;
        } else if (!thinking.isEmpty()) {
            displayText = "【深度思考】\n" + thinking;
        } else {
            displayText = "抱歉，我刚才没有成功生成可读回复。";
        }

        return new FormatResult(thinking, answer, displayText);
    }

    private static boolean looksLikeSdkTrace(String raw) {
        return raw.contains("ItemOutputMessage{")
                || raw.contains("OutputContentItemText")
                || raw.contains("status='")
                || raw.contains("partial=");
    }

    @NonNull
    private static String extractBestAnswer(@NonNull String raw) {
        List<String> allTexts = captureList(GENERIC_TEXT_PATTERN, raw);
        if (allTexts.isEmpty()) {
            return "";
        }
        // 通常最后一个 text 更接近 assistant 最终输出
        return allTexts.get(allTexts.size() - 1);
    }

    @NonNull
    private static String joinCaptured(@NonNull Pattern pattern, @NonNull String source) {
        List<String> parts = captureList(pattern, source);
        if (parts.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String p : parts) {
            String cleaned = cleanupText(p);
            if (cleaned.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(cleaned);
        }
        return builder.toString();
    }

    @NonNull
    private static List<String> captureList(@NonNull Pattern pattern, @NonNull String source) {
        List<String> list = new ArrayList<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            list.add(unescape(matcher.group(1)));
        }
        return list;
    }

    @NonNull
    private static String cleanupText(@NonNull String text) {
        String result = unescape(text);

        // 隐藏回答过程中的调用痕迹/结构化调试片段
        result = result.replaceAll("(?s)Item[A-Za-z]+\\{.*?\\}", " ");
        result = result.replaceAll("type='[^']*'", " ");
        result = result.replaceAll("status='[^']*'", " ");
        result = result.replaceAll("id='[^']*'", " ");
        result = result.replaceAll("partial=[^,}\\]]+", " ");
        result = result.replaceAll("annotations=[^,}\\]]+", " ");
        result = result.replaceAll("content=\\[[^\\]]*\\]", " ");
        result = result.replaceAll("output_text", " ");

        // 隐藏代码块/命令块
        result = CODE_BLOCK_PATTERN.matcher(result).replaceAll(" ");
        result = result.replaceAll("`[^`]*`", " ");

        result = result.replace("\\r", "");
        result = result.replaceAll("\\s*\\n\\s*", "\n");
        result = result.replaceAll("[ \\t]{2,}", " ");
        result = result.replaceAll("\\n{3,}", "\n\n");
        return result.trim();
    }

    @NonNull
    private static String unescape(String input) {
        return input
                .replace("\\\\n", "\n")
                .replace("\\\\t", "\t")
                .replace("\\\\r", "")
                .replace("\\\\'", "'")
                .replace("\\\\\"", "\"")
                .trim();
    }

    public static final class FormatResult {
        public final String thinking;
        public final String answer;
        public final String displayText;

        public FormatResult(String thinking, String answer, String displayText) {
            this.thinking = thinking;
            this.answer = answer;
            this.displayText = displayText;
        }
    }
}
