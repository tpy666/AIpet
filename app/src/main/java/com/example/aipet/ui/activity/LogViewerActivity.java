package com.example.aipet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.aipet.R;
import com.example.aipet.util.ChatLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志查看器活动
 * 
 * 功能：
 * • 实时显示应用日志
 * • 按日志等级筛选
 * • 按关键字搜索
 * • 导出日志
 * • 清空日志
 */
public class LogViewerActivity extends AppCompatActivity {
    
    private static final String TAG = "LogViewerActivity";
    
    private ListView logListView;
    private EditText searchEditText;
    private ArrayAdapter<String> adapter;
    private List<ChatLogger.LogEntry> allLogs;
    private List<String> displayLogs;
    private int currentFilterLevel = ChatLogger.LEVEL_DEBUG;
    private ChatLogger chatLogger;
    
    // 过滤选项
    private static final int FILTER_ALL = 0;
    private static final int FILTER_DEBUG = 1;
    private static final int FILTER_INFO = 2;
    private static final int FILTER_WARNING = 3;
    private static final int FILTER_ERROR = 4;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);
        
        initializeUI();
        initializeLogger();
        loadLogs();
    }
    
    /**
     * 初始化 UI
     */
    private void initializeUI() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("📋 聊天日志");
        
        // 初始化列表视图
        logListView = findViewById(R.id.log_list_view);
        displayLogs = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayLogs);
        logListView.setAdapter(adapter);
        
        // 设置日志条目的点击事件
        logListView.setOnItemClickListener((parent, view, position, id) -> {
            String logText = displayLogs.get(position);
            showLogDetail(logText);
        });
        
        // 初始化搜索框
        searchEditText = findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * 初始化日志记录器
     */
    private void initializeLogger() {
        chatLogger = ChatLogger.getInstance(this);
        allLogs = new ArrayList<>();
    }
    
    /**
     * 加载日志
     */
    private void loadLogs() {
        try {
            allLogs = chatLogger.getMemoryLogs();
            filterLogs();
        } catch (Exception e) {
            showToast("加载日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 过滤日志
     */
    private void filterLogs() {
        displayLogs.clear();
        String searchText = searchEditText.getText().toString().toLowerCase();
        
        for (ChatLogger.LogEntry log : allLogs) {
            // 检查日志等级
            if (currentFilterLevel != FILTER_ALL && log.level < currentFilterLevel) {
                continue;
            }
            
            // 检查搜索关键字
            if (!searchText.isEmpty()) {
                String logText = log.toString().toLowerCase();
                if (!logText.contains(searchText)) {
                    continue;
                }
            }
            
            displayLogs.add(log.toString());
        }
        
        // 向下滚动到最新日志
        adapter.notifyDataSetChanged();
        if (displayLogs.size() > 0) {
            logListView.setSelection(displayLogs.size() - 1);
        }
    }
    
    /**
     * 显示日志详情
     */
    private void showLogDetail(String logText) {
        // 使用对话框显示完整日志
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("📄 日志详情")
                .setMessage(logText)
                .setPositiveButton("复制", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = 
                        (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("log", logText);
                    clipboard.setPrimaryClip(clip);
                    showToast("已复制到剪贴板");
                })
                .setNegativeButton("关闭", null)
                .show();
    }
    
    /**
     * 显示提示信息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log_viewer, menu);
        return true;
    }
    
    /**
     * 处理菜单项选择
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.menu_filter_all) {
            currentFilterLevel = FILTER_ALL;
            filterLogs();
            showToast("显示所有日志");
            return true;
        } else if (itemId == R.id.menu_filter_debug) {
            currentFilterLevel = ChatLogger.LEVEL_DEBUG;
            filterLogs();
            showToast("仅显示 DEBUG 日志");
            return true;
        } else if (itemId == R.id.menu_filter_info) {
            currentFilterLevel = ChatLogger.LEVEL_INFO;
            filterLogs();
            showToast("仅显示 INFO 及以上日志");
            return true;
        } else if (itemId == R.id.menu_filter_warning) {
            currentFilterLevel = ChatLogger.LEVEL_WARNING;
            filterLogs();
            showToast("仅显示 WARNING 及以上日志");
            return true;
        } else if (itemId == R.id.menu_filter_error) {
            currentFilterLevel = ChatLogger.LEVEL_ERROR;
            filterLogs();
            showToast("仅显示 ERROR 日志");
            return true;
        } else if (itemId == R.id.menu_clear_logs) {
            clearLogs();
            return true;
        } else if (itemId == R.id.menu_export_logs) {
            exportLogs();
            return true;
        } else if (itemId == R.id.menu_refresh) {
            loadLogs();
            showToast("刷新完成");
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 清空日志
     */
    private void clearLogs() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ 确认")
                .setMessage("确定要清空所有日志吗？此操作无法撤销。")
                .setPositiveButton("确定", (dialog, which) -> {
                    try {
                        chatLogger.clearMemoryLogs();
                        chatLogger.clearAllLogFiles();
                        allLogs.clear();
                        displayLogs.clear();
                        adapter.notifyDataSetChanged();
                        showToast("日志已清空");
                    } catch (Exception e) {
                        showToast("清空日志失败: " + e.getMessage());
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    /**
     * 导出日志
     */
    private void exportLogs() {
        try {
            String logContent = chatLogger.exportLogs();
            
            // 创建意图以分享日志
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "AIpet 聊天日志");
            intent.putExtra(Intent.EXTRA_TEXT, logContent);
            
            startActivity(Intent.createChooser(intent, "📤 导出日志"));
        } catch (Exception e) {
            showToast("导出日志失败: " + e.getMessage());
        }
    }
}
