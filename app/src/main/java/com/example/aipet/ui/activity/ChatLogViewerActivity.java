package com.example.aipet.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aipet.R;
import com.example.aipet.util.ChatLogger;
import com.example.aipet.util.Constants;

import java.util.List;
import java.util.Locale;

/**
 * 聊天日志页（新版）
 * - 回答内容默认展示
 * - 深度思考可折叠查看，默认隐藏
 */
public class ChatLogViewerActivity extends BaseActivity {

    private LinearLayout layoutEntries;
    private ScrollView svLogs;
    private Button btnRefresh;
    private Button btnCopy;
    private Button btnClear;
    private ChatLogger chatLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_log_viewer);
        setupScreen("聊天日志", true);

        chatLogger = ChatLogger.getInstance(this);
        layoutEntries = bind(R.id.layout_chat_log_entries);
        svLogs = bind(R.id.sv_chat_logs);
        btnRefresh = bind(R.id.btn_refresh_chat_logs);
        btnCopy = bind(R.id.btn_copy_chat_logs);
        btnClear = bind(R.id.btn_clear_chat_logs);

        btnRefresh.setOnClickListener(v -> renderLogs());
        btnCopy.setOnClickListener(v -> copyLogs());
        btnClear.setOnClickListener(v -> clearLogs());

        renderLogs();
    }

    private void renderLogs() {
        layoutEntries.removeAllViews();
        List<ChatLogger.LogEntry> logs = chatLogger.getChatLogEntries();
        if (logs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无聊天日志\n\n开始聊天后会在这里显示。");
            empty.setTextSize(13f);
            empty.setTextColor(getColor(android.R.color.darker_gray));
            layoutEntries.addView(empty);
            return;
        }

        for (ChatLogger.LogEntry entry : logs) {
            View row = getLayoutInflater().inflate(R.layout.item_chat_log_entry, layoutEntries, false);
            TextView tvHeader = row.findViewById(R.id.tv_log_header);
            TextView tvAnswer = row.findViewById(R.id.tv_log_answer);
            TextView tvThinkingToggle = row.findViewById(R.id.tv_log_thinking_toggle);
            TextView tvThinking = row.findViewById(R.id.tv_log_thinking);

            tvHeader.setText(String.format(Locale.CHINA, "[%s] %s", entry.timestamp, entry.tag));

            ParsedAnswer parsed = parseAnswer(entry);
            tvAnswer.setText(parsed.answer);

            if (parsed.hasThinking()) {
                tvThinkingToggle.setVisibility(View.VISIBLE);
                tvThinking.setVisibility(View.GONE);
                tvThinking.setText(parsed.thinking);
                tvThinkingToggle.setText(R.string.chat_thinking_expand);
                tvThinkingToggle.setOnClickListener(v -> {
                    boolean expand = tvThinking.getVisibility() != View.VISIBLE;
                    tvThinking.setVisibility(expand ? View.VISIBLE : View.GONE);
                    tvThinkingToggle.setText(expand ? R.string.chat_thinking_collapse : R.string.chat_thinking_expand);
                });
            } else {
                tvThinkingToggle.setVisibility(View.GONE);
                tvThinking.setVisibility(View.GONE);
            }

            layoutEntries.addView(row);
        }

        svLogs.post(() -> svLogs.fullScroll(View.FOCUS_DOWN));
    }

    private ParsedAnswer parseAnswer(@NonNull ChatLogger.LogEntry entry) {
        if (!Constants.LOG_TAG_API_ANSWER.equals(entry.tag)) {
            return new ParsedAnswer(entry.message, "");
        }

        String source = entry.message == null ? "" : entry.message;
        String answerTitle = "【回答】";
        String thinkingTitle = "【深度思考】";

        int answerIndex = source.indexOf(answerTitle);
        int thinkingIndex = source.indexOf(thinkingTitle);

        if (answerIndex < 0) {
            return new ParsedAnswer(source, "");
        }

        String answer;
        String thinking = "";
        if (thinkingIndex > answerIndex) {
            answer = source.substring(answerIndex + answerTitle.length(), thinkingIndex).trim();
            thinking = source.substring(thinkingIndex + thinkingTitle.length()).trim();
        } else {
            answer = source.substring(answerIndex + answerTitle.length()).trim();
        }
        return new ParsedAnswer(answer, thinking);
    }

    private void copyLogs() {
        String content = chatLogger.getChatLogContent();
        if (content.trim().isEmpty()) {
            showToast("暂无内容可复制");
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("chat_logs", content));
            showToast("聊天日志已复制");
        }
    }

    private void clearLogs() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("清空聊天日志")
                .setMessage("确定要清空所有聊天日志吗？")
                .setPositiveButton("清空", (dialog, which) -> {
                    chatLogger.clearAllLogs();
                    renderLogs();
                    showToast("聊天日志已清空");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private static final class ParsedAnswer {
        final String answer;
        final String thinking;

        ParsedAnswer(String answer, String thinking) {
            this.answer = answer == null ? "" : answer;
            this.thinking = thinking == null ? "" : thinking;
        }

        boolean hasThinking() {
            return !thinking.trim().isEmpty();
        }
    }
}
