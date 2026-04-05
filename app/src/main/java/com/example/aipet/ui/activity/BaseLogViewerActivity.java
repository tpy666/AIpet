package com.example.aipet.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.aipet.util.ChatLogger;

/**
 * 日志页面基础模板
 *
 * 提供刷新、复制、清空和统一状态处理，
 * 子类只负责提供日志内容和页面文案。
 */
public abstract class BaseLogViewerActivity extends BaseActivity {

    protected TextView tvLogs;
    protected Button btnRefresh;
    protected Button btnClear;
    protected Button btnCopy;
    protected ScrollView svLogs;
    protected ChatLogger chatLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        setupScreen(getScreenTitle(), true);

        chatLogger = ChatLogger.getInstance(this);
        initCommonViews();
        loadLogs();
    }

    @LayoutRes
    protected abstract int getLayoutResId();

    protected abstract String getScreenTitle();

    protected abstract String getLogContent();

    protected abstract String getEmptyHintText();

    protected abstract String getClipLabel();

    protected abstract String getConfirmTitle();

    protected abstract String getConfirmMessage();

    protected abstract String getClearSuccessMessage();

    protected abstract void clearLogsInternal();

    @IdRes
    protected abstract int getLogTextViewId();

    @IdRes
    protected abstract int getRefreshButtonId();

    @IdRes
    protected abstract int getCopyButtonId();

    @IdRes
    protected abstract int getClearButtonId();

    @IdRes
    protected abstract int getScrollViewId();

    @ColorRes
    protected int getLogTextColorRes(boolean isEmpty) {
        return isEmpty ? android.R.color.darker_gray : android.R.color.black;
    }

    protected boolean scrollToBottom() {
        return true;
    }

    private void initCommonViews() {
        tvLogs = bind(getLogTextViewId());
        btnRefresh = bind(getRefreshButtonId());
        btnClear = bind(getClearButtonId());
        btnCopy = bind(getCopyButtonId());
        svLogs = bind(getScrollViewId());

        btnRefresh.setOnClickListener(v -> loadLogs());
        btnClear.setOnClickListener(v -> confirmClearLogs());
        btnCopy.setOnClickListener(v -> copyToClipboard());
    }

    protected void loadLogs() {
        String content = getLogContent();
        boolean isEmpty = content == null || content.trim().isEmpty();

        tvLogs.setText(isEmpty ? getEmptyHintText() : content);
        tvLogs.setTextColor(ContextCompat.getColor(this, getLogTextColorRes(isEmpty)));

        if (scrollToBottom()) {
            svLogs.post(() -> svLogs.fullScroll(ScrollView.FOCUS_DOWN));
        } else {
            svLogs.post(() -> svLogs.scrollTo(0, 0));
        }
    }

    private void copyToClipboard() {
        String content = tvLogs.getText().toString();
        if (content.trim().isEmpty()) {
            showToast("暂无内容可复制");
            return;
        }

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getClipLabel(), content);
        clipboardManager.setPrimaryClip(clipData);
        showToast("日志已复制到剪贴板");
    }

    private void confirmClearLogs() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getConfirmTitle())
                .setMessage(getConfirmMessage())
                .setPositiveButton("清空", (dialog, which) -> {
                    clearLogsInternal();
                    loadLogs();
                    showToast(getClearSuccessMessage());
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
