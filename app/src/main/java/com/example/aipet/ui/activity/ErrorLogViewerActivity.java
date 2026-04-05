package com.example.aipet.ui.activity;

import com.example.aipet.R;

/**
 * API 错误日志查看器
 */
public class ErrorLogViewerActivity extends BaseLogViewerActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_error_log_viewer;
    }

    @Override
    protected String getScreenTitle() {
        return "API 错误日志";
    }

    @Override
    protected String getLogContent() {
        return chatLogger.getErrorLogContent();
    }

    @Override
    protected String getEmptyHintText() {
        return "暂无 API 错误日志\n\n这是一个很好的信号，表示 API 请求运行正常。";
    }

    @Override
    protected String getClipLabel() {
        return "error_logs";
    }

    @Override
    protected String getConfirmTitle() {
        return "清空错误日志";
    }

    @Override
    protected String getConfirmMessage() {
        return "确定要清空所有 API 错误日志吗？此操作无法撤销。";
    }

    @Override
    protected String getClearSuccessMessage() {
        return "错误日志已清空";
    }

    @Override
    protected void clearLogsInternal() {
        chatLogger.clearErrorLogs();
    }

    @Override
    protected int getLogTextViewId() {
        return R.id.tv_error_logs;
    }

    @Override
    protected int getRefreshButtonId() {
        return R.id.btn_refresh_error_logs;
    }

    @Override
    protected int getCopyButtonId() {
        return R.id.btn_copy_error_logs;
    }

    @Override
    protected int getClearButtonId() {
        return R.id.btn_clear_error_logs;
    }

    @Override
    protected int getScrollViewId() {
        return R.id.sv_error_logs;
    }

    @Override
    protected int getLogTextColorRes(boolean isEmpty) {
        return isEmpty ? android.R.color.holo_green_dark : android.R.color.holo_red_dark;
    }

    @Override
    protected boolean scrollToBottom() {
        return false;
    }
}
