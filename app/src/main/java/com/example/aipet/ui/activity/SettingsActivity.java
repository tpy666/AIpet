package com.example.aipet.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.aipet.R;
import com.example.aipet.network.ApiClient;
import com.example.aipet.network.ApiConfig;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.Constants;
import com.example.aipet.util.UtilHub;
import com.example.aipet.network.request.ApiConnectionTester;
import com.example.aipet.util.store.ApiSettingsStore;

/**
 * 设置页面 - 配置 API 端点和密钥
 * 支持多种 API 提供商配置
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private RadioGroup rgApiProvider;
    private EditText etApiUrl;
    private EditText etApiKey;
    private EditText etModelName;
    private Button btnSave;
    private Button btnTest;
    private Button btnReset;
    private Button btnViewChatLogs;
    private Button btnViewErrorLogs;
    private TextView tvStatus;

    private ApiSettingsStore settingsStore;
    private boolean suppressProviderChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_settings);
            setupScreen("API 设置", true);

            initViews();
            loadSettings();
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error in onCreate", e);
            Toast.makeText(this, "初始化设置页面失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            rgApiProvider = bind(R.id.rg_api_provider);
            etApiUrl = bind(R.id.et_api_url);
            etApiKey = bind(R.id.et_api_key);
            etModelName = bind(R.id.et_model_name);
            btnSave = bind(R.id.btn_save);
            btnTest = bind(R.id.btn_test);
            btnReset = bind(R.id.btn_reset);
            btnViewChatLogs = bind(R.id.btn_view_chat_logs);
            btnViewErrorLogs = bind(R.id.btn_view_error_logs);
            tvStatus = bind(R.id.tv_status);

            settingsStore = UtilHub.apiSettingsStore(this);

            // 提供方选择监听
            rgApiProvider.setOnCheckedChangeListener((group, checkedId) -> {
                if (!suppressProviderChange) {
                    onProviderChanged(checkedId, true);
                }
            });

            // 保存按钮
            btnSave.setOnClickListener(v -> saveSettings());

            // 测试连接按钮
            btnTest.setOnClickListener(v -> testConnection());

            // 重置按钮
            btnReset.setOnClickListener(v -> resetToDefaults());

            // 查看聊天日志按钮
            btnViewChatLogs.setOnClickListener(v -> viewChatLogs());

            // 查看错误日志按钮
            btnViewErrorLogs.setOnClickListener(v -> viewErrorLogs());
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error in initViews", e);
            Toast.makeText(this, "初始化控件失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 当 API 提供方改变时
     */
    private void onProviderChanged(int checkedId) {
        onProviderChanged(checkedId, true);
    }

    private void onProviderChanged(int checkedId, boolean clearInputs) {
        try {
            if (checkedId == R.id.rb_openai) {
                if (clearInputs) {
                    etApiUrl.setText("");
                    etApiKey.setText("");
                    etModelName.setText(Constants.OPENAI_DEFAULT_MODEL);
                }
                etApiUrl.setHint(Constants.OPENAI_BASE_URL);
                etApiKey.setHint("sk-...");
                tvStatus.setText("状态：OpenAI - 需要有效的 API Key");
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            } else if (checkedId == R.id.rb_doubao) {
                if (clearInputs) {
                    etApiUrl.setText("");
                    etApiKey.setText("");
                    etModelName.setText(Constants.DOUBAO_DEFAULT_MODEL);
                }
                etApiUrl.setHint(Constants.DOUBAO_BASE_URL);
                etApiKey.setHint("your-bearer-token");
                tvStatus.setText("状态：豆包 API - 多模态支持（文本+图片）");
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            } else if (checkedId == R.id.rb_local) {
                if (clearInputs) {
                    etApiUrl.setText("");
                    etApiKey.setText("");
                    etModelName.setText(Constants.LOCAL_BACKEND_DEFAULT_MODEL);
                }
                etApiUrl.setHint(Constants.LOCAL_BACKEND_URL);
                etApiKey.setHint("(可选)");
                tvStatus.setText("状态：本地后端 - 确保后端服务正在运行");
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            } else if (checkedId == R.id.rb_custom) {
                if (clearInputs) {
                    etApiUrl.setText("");
                    etApiKey.setText("");
                    etModelName.setText("");
                }
                etApiUrl.setHint(Constants.CUSTOM_API_EXAMPLE_URL);
                etApiKey.setHint("your-api-key");
                etModelName.setHint("your-model");
                tvStatus.setText("状态：自定义 API - 请确保 URL 有效");
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_purple));
            }
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error in onProviderChanged", e);
            tvStatus.setText("状态：设置错误");
        }
    }

    /**
     * 加载已保存的设置
     */
    private void loadSettings() {
        ApiSettingsStore.ApiSettings settings = settingsStore.load();
        String provider = settings.provider;
        String apiUrl = settings.apiUrl;
        String apiKey = settings.apiKey;
        String modelName = settings.modelName;

        suppressProviderChange = true;
        try {
            int checkedId = resolveProviderRadioId(provider);
            rgApiProvider.check(checkedId);
            etApiUrl.setText(apiUrl);
            etApiKey.setText(apiKey);
            etModelName.setText(modelName);
            onProviderChanged(checkedId, false);
        } finally {
            suppressProviderChange = false;
        }
    }

    private int resolveProviderRadioId(String provider) {
        if (Constants.PROVIDER_DOUBAO.equals(provider)) {
            return R.id.rb_doubao;
        }
        if (Constants.PROVIDER_LOCAL.equals(provider)) {
            return R.id.rb_local;
        }
        if (Constants.PROVIDER_CUSTOM.equals(provider)) {
            return R.id.rb_custom;
        }
        return R.id.rb_openai;
    }

    /**
     * 保存设置（带完整错误处理）
     */
    private void saveSettings() {
        try {
            String apiUrl = etApiUrl.getText().toString().trim();
            String apiKey = etApiKey.getText().toString().trim();
            String modelName = etModelName.getText().toString().trim();

            // 验证输入
            int checkedId = rgApiProvider.getCheckedRadioButtonId();
            String provider = resolveProviderValue(checkedId);
            apiUrl = resolveApiUrl(provider, apiUrl);
            modelName = resolveModelName(provider, modelName);

            if (Constants.PROVIDER_CUSTOM.equals(provider) && apiUrl.isEmpty()) {
                showError("API URL 不能为空");
                return;
            }

            if (!Constants.PROVIDER_LOCAL.equals(provider) && apiKey.isEmpty()) {
                showError("API Key 不能为空");
                return;
            }

            if (modelName.isEmpty()) {
                showError("模型名称不能为空");
                return;
            }

            settingsStore.save(new ApiSettingsStore.ApiSettings(provider, apiUrl, apiKey, modelName));

            // 第二步：更新 ApiConfig（可能抛出异常）
            try {
                if (Constants.PROVIDER_OPENAI.equals(provider)) {
                    ApiConfig.getInstance().configureOpenAI(apiKey, modelName);
                } else if (Constants.PROVIDER_DOUBAO.equals(provider)) {
                    ApiClient.configureDoubao(apiUrl, apiKey, modelName);
                } else if (Constants.PROVIDER_LOCAL.equals(provider)) {
                    ApiConfig.getInstance().configureLocalBackend(apiUrl);
                } else {
                    ApiConfig.getInstance().configureCustom(apiUrl, apiKey, modelName);
                }
            } catch (Exception e) {
                Log.e(TAG, "API 配置失败: " + e.getMessage(), e);
                // 即使 API 配置失败，设置已持久化，不中断流程
                // 用户可以重新尝试或手动调整设置
            }

            // 第三步：更新 UI 状态
            runOnUiThread(() -> {
                tvStatus.setText("✓ 设置已保存");
                tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                Toast.makeText(this, "设置已保存，请尝试连接", Toast.LENGTH_SHORT).show();
            });
            
            Log.i(TAG, "设置已成功保存: Provider=" + provider);

        } catch (Exception e) {
            Log.e(TAG, "保存设置时出错: " + e.getMessage(), e);
            showError("保存设置失败: " + e.getMessage());
        }
    }

    private String resolveProviderValue(int checkedId) {
        if (checkedId == R.id.rb_doubao) {
            return Constants.PROVIDER_DOUBAO;
        }
        if (checkedId == R.id.rb_local) {
            return Constants.PROVIDER_LOCAL;
        }
        if (checkedId == R.id.rb_custom) {
            return Constants.PROVIDER_CUSTOM;
        }
        return Constants.PROVIDER_OPENAI;
    }

    private String resolveApiUrl(String provider, String inputUrl) {
        if (!inputUrl.isEmpty()) {
            return inputUrl;
        }
        if (Constants.PROVIDER_OPENAI.equals(provider)) {
            return Constants.OPENAI_BASE_URL;
        }
        if (Constants.PROVIDER_DOUBAO.equals(provider)) {
            return Constants.DOUBAO_BASE_URL;
        }
        if (Constants.PROVIDER_LOCAL.equals(provider)) {
            return Constants.LOCAL_BACKEND_URL;
        }
        return inputUrl;
    }

    private String resolveModelName(String provider, String inputModelName) {
        if (!inputModelName.isEmpty()) {
            return inputModelName;
        }
        if (Constants.PROVIDER_OPENAI.equals(provider)) {
            return Constants.OPENAI_DEFAULT_MODEL;
        }
        if (Constants.PROVIDER_DOUBAO.equals(provider)) {
            return Constants.DOUBAO_DEFAULT_MODEL;
        }
        if (Constants.PROVIDER_LOCAL.equals(provider)) {
            return Constants.LOCAL_BACKEND_DEFAULT_MODEL;
        }
        return inputModelName;
    }

    /**
     * 显示错误提示
     */
    private void showError(String message) {
        tvStatus.setText("✗ " + message);
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.w(TAG, "Settings Error: " + message);
    }

    /**
     * 测试 API 连接
     */
    private void testConnection() {
        String apiUrl = etApiUrl.getText().toString().trim();
        String apiKey = etApiKey.getText().toString().trim();
        String modelName = etModelName.getText().toString().trim();
        int selectedProvider = rgApiProvider.getCheckedRadioButtonId();

        apiUrl = resolveApiUrl(resolveProviderValue(selectedProvider), apiUrl);
        modelName = resolveModelName(resolveProviderValue(selectedProvider), modelName);

        final String finalApiUrl = apiUrl;
        final String finalModelName = modelName;
        final int finalSelectedProvider = selectedProvider;

        if (finalApiUrl.isEmpty() && finalSelectedProvider == R.id.rb_custom) {
            Toast.makeText(this, "请先填入 API URL", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("⏳ 正在测试连接...");
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        btnTest.setEnabled(false);

        ApiConnectionTester.testConnection(
                resolveProviderValue(finalSelectedProvider),
                finalApiUrl,
                apiKey,
                finalModelName,
                new ApiConnectionTester.Callback() {
                    @Override
                    public void onSuccess(@NonNull String message) {
                        runOnUiThread(() -> {
                            tvStatus.setText(message);
                            tvStatus.setTextColor(ContextCompat.getColor(SettingsActivity.this, android.R.color.holo_green_dark));
                            Toast.makeText(SettingsActivity.this, "✓ API 连接测试成功", Toast.LENGTH_SHORT).show();
                            btnTest.setEnabled(true);
                        });
                    }

                    @Override
                    public void onFailure(@NonNull String message) {
                        runOnUiThread(() -> {
                            tvStatus.setText("✗ " + message);
                            tvStatus.setTextColor(ContextCompat.getColor(SettingsActivity.this, android.R.color.holo_red_dark));
                            Toast.makeText(SettingsActivity.this, "✗ " + message, Toast.LENGTH_SHORT).show();
                            btnTest.setEnabled(true);
                        });
                    }
                }
        );
    }

    /**
     * 重置为默认设置
     */
    private void resetToDefaults() {
        try {
            rgApiProvider.check(R.id.rb_openai);
            etApiUrl.setText(Constants.OPENAI_BASE_URL);
            etApiKey.setText("");
            etModelName.setText(Constants.OPENAI_DEFAULT_MODEL);
            
            updateResetUI();
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error in resetToDefaults", e);
            Toast.makeText(this, "重置失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 查看聊天日志
     */
    private void viewChatLogs() {
        navigateTo(UiNavigator.toChatLogs(this));
    }

    /**
     * 查看 API 错误日志
     */
    private void viewErrorLogs() {
        navigateTo(UiNavigator.toErrorLogs(this));
    }

    /**
     * 重置后更新 UI
     */
    private void updateResetUI() {
        tvStatus.setText("✓ 已重置为默认设置");
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        Toast.makeText(this, "已重置为默认设置", Toast.LENGTH_SHORT).show();
    }

    /**
     * 验证 URL 格式
     */
    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

}
