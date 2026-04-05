package com.example.aipet.ui.activity;

import com.example.aipet.R;

/**
 * 聊天日志查看器
 */
public class ChatLogViewerActivity extends BaseLogViewerActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_chat_log_viewer;
    }

    @Override
    protected String getScreenTitle() {
        return "聊天日志";
    }

    @Override
    protected String getLogContent() {
        return chatLogger.getChatLogContent();
    }

    @Override
    protected String getEmptyHintText() {
        return "暂无聊天日志\n\n开始与宠物聊天后，所有消息都会记录在这里。";
    }

    @Override
    protected String getClipLabel() {
        return "chat_logs";
    }

    @Override
    protected String getConfirmTitle() {
        return "清空聊天日志";
    }

    @Override
    protected String getConfirmMessage() {
        return "确定要清空所有聊天日志吗？此操作无法撤销。";
    }

    @Override
    protected String getClearSuccessMessage() {
        return "聊天日志已清空";
    }

    @Override
    protected void clearLogsInternal() {
        chatLogger.clearAllLogs();
    }

    @Override
    protected int getLogTextViewId() {
        return R.id.tv_chat_logs;
    }

    @Override
    protected int getRefreshButtonId() {
        return R.id.btn_refresh_chat_logs;
    }

    @Override
    protected int getCopyButtonId() {
        return R.id.btn_copy_chat_logs;
    }

    @Override
    protected int getClearButtonId() {
        return R.id.btn_clear_chat_logs;
    }

    @Override
    protected int getScrollViewId() {
        return R.id.sv_chat_logs;
    }

    @Override
    protected int getLogTextColorRes(boolean isEmpty) {
        return android.R.color.darker_gray;
    }
}
