package com.example.aipet.network;

import androidx.annotation.NonNull;

/**
 * API 回答接收端口：统一承载思考与回答。
 */
public final class APIAnswer {

    public final String thinking;
    public final String answer;
    public final String raw;

    public APIAnswer(@NonNull String thinking, @NonNull String answer, @NonNull String raw) {
        this.thinking = thinking;
        this.answer = answer;
        this.raw = raw;
    }

    @NonNull
    public static APIAnswer fromRaw(@NonNull String rawReply) {
        AssistantReplyFormatter.FormatResult result = AssistantReplyFormatter.format(rawReply);
        String cleanedAnswer = result.answer == null ? "" : result.answer.trim();
        if (cleanedAnswer.isEmpty()) {
            cleanedAnswer = result.displayText == null ? "" : result.displayText.trim();
        }
        return new APIAnswer(
                result.thinking == null ? "" : result.thinking.trim(),
                cleanedAnswer,
                rawReply == null ? "" : rawReply.trim()
        );
    }

    @NonNull
    public String answerOnly() {
        if (answer == null || answer.trim().isEmpty()) {
            return "抱歉，我刚刚没有成功生成有效回答。";
        }
        return answer.trim();
    }

    @NonNull
    public String toLogBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("【回答】\n").append(answerOnly());
        if (thinking != null && !thinking.trim().isEmpty()) {
            sb.append("\n\n【深度思考】\n").append(thinking.trim());
        }
        return sb.toString();
    }
}
