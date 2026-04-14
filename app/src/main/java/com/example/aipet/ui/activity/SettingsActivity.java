package com.example.aipet.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;
import com.example.aipet.network.ApiClient;
import com.example.aipet.network.ApiConfig;
import com.example.aipet.network.request.ApiConnectionTester;
import com.example.aipet.ui.avatar.AvatarDiffStore;
import com.example.aipet.ui.avatar.AvatarImagePipeline;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.Constants;
import com.example.aipet.util.SPUtils;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.ApiSettingsStore;
import com.example.aipet.util.store.OutingBackgroundStore;
import com.example.aipet.util.store.PetStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置页面：API、图片、角色卡和日志均以折叠区展示。
 */
public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";
    public static final String EXTRA_OPEN_SECTION = "extra_open_section";
    private static final String SECTION_ROLE = "role";
    private static final String SECTION_IMAGE = "image";
    private static final String SECTION_OUTING = "outing";
    private static final String SECTION_API = "api";
    private static final String SECTION_LOG = "log";

    private RadioGroup rgApiProvider;
    private EditText etApiUrl;
    private EditText etApiKey;
    private EditText etModelName;
    private EditText etAvatarImageUrl;
    private EditText etAvatarUploadEndpoint;
    private EditText etAvatarRemoveBgEndpoint;
    private CheckBox cbAvatarAutoProcess;
    private CheckBox cbAvatarPreserveOriginal;
    private EditText etOutingBgPlaceName;
    private EditText etOutingBgEnvironment;
    private EditText etOutingBgImageUrl;
    private EditText etOutingBgDelta;
    private Button btnSave;
    private Button btnTest;
    private Button btnReset;
    private Button btnViewChatLogs;
    private Button btnViewErrorLogs;
    private Button btnCreatePetCard;
    private Button btnManagePetCards;
    private Button btnApplyAvatar;
    private Button btnPickAvatarImage;
    private Button btnPickAvatarFromAssets;
    private Button btnSelectDiffOutfit;
    private Button btnUploadAvatarDiff;
    private Button btnAddOutingBackground;
    private Button btnPickOutingBackgroundImage;
    private Button btnHelpPage;
    private Button btnDressUpPage;
    private TextView tvStatus;
    private TextView tvAvatarStatus;
    private TextView tvOutingStatus;
    private TextView tvApiFoldToggle;
    private TextView tvRoleFoldToggle;
    private TextView tvImageFoldToggle;
    private TextView tvOutingFoldToggle;
    private TextView tvLogFoldToggle;
    private View layoutRoleHeader;
    private View layoutRoleContent;
    private View layoutImageHeader;
    private View layoutImageContent;
    private View layoutOutingHeader;
    private View layoutOutingContent;
    private View layoutApiHeader;
    private View layoutApiContent;
    private View layoutLogHeader;
    private View layoutLogContent;
    private RecyclerView rvOutingBackgrounds;

    private ApiSettingsStore settingsStore;
    private OutingBackgroundStore outingBackgroundStore;
    private PetStore petStore;
    private OutingBackgroundAdapter outingBackgroundAdapter;
    private ActivityResultLauncher<String[]> outingBackgroundPickerLauncher;
    private ActivityResultLauncher<String[]> avatarPickerLauncher;
    private ActivityResultLauncher<String[]> avatarDiffPickerLauncher;
    private String selectedDiffOutfitId;
    private String selectedDiffOutfitName;
    private String pendingDiffOutfitId;
    private String pendingDiffEmotion;
    private boolean suppressProviderChange;
    private boolean roleExpanded;
    private boolean imageExpanded;
    private boolean outingExpanded;
    private boolean apiExpanded;
    private boolean logExpanded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupScreen(getString(R.string.settings_title), true);

        settingsStore = UtilHub.apiSettingsStore(this);
        outingBackgroundStore = UtilHub.outingBackgroundStore(this);
        petStore = UtilHub.petStore(this);
        avatarPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::onAvatarImagePicked
        );
        outingBackgroundPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::onOutingBackgroundPicked
        );
        avatarDiffPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            this::onAvatarDiffImagePicked
        );
        initViews();
        loadSettings();
        openRequestedSection();
    }

    private void initViews() {
        rgApiProvider = bind(R.id.rg_api_provider);
        etApiUrl = bind(R.id.et_api_url);
        etApiKey = bind(R.id.et_api_key);
        etModelName = bind(R.id.et_model_name);
        etAvatarImageUrl = bind(R.id.et_avatar_image_url);
        etAvatarUploadEndpoint = bind(R.id.et_avatar_upload_endpoint);
        etAvatarRemoveBgEndpoint = bind(R.id.et_avatar_remove_bg_endpoint);
        cbAvatarAutoProcess = bind(R.id.cb_avatar_auto_process);
        cbAvatarPreserveOriginal = bind(R.id.cb_avatar_preserve_original);
        etOutingBgPlaceName = bind(R.id.et_outing_bg_place_name);
        etOutingBgEnvironment = bind(R.id.et_outing_bg_environment);
        etOutingBgImageUrl = bind(R.id.et_outing_bg_image_url);
        etOutingBgDelta = bind(R.id.et_outing_bg_delta);
        btnSave = bind(R.id.btn_save);
        btnTest = bind(R.id.btn_test);
        btnReset = bind(R.id.btn_reset);
        btnViewChatLogs = bind(R.id.btn_view_chat_logs);
        btnViewErrorLogs = bind(R.id.btn_view_error_logs);
        btnCreatePetCard = bind(R.id.btn_create_pet_card);
        btnManagePetCards = bind(R.id.btn_manage_pet_cards);
        btnApplyAvatar = bind(R.id.btn_apply_avatar);
        btnPickAvatarImage = bind(R.id.btn_pick_avatar_image);
        btnPickAvatarFromAssets = bind(R.id.btn_pick_avatar_asset);
        btnSelectDiffOutfit = bind(R.id.btn_select_avatar_diff_outfit);
        btnUploadAvatarDiff = bind(R.id.btn_upload_avatar_diff);
        btnPickOutingBackgroundImage = bind(R.id.btn_pick_outing_bg_image);
        btnAddOutingBackground = bind(R.id.btn_add_outing_background);
        btnHelpPage = bind(R.id.btn_help_page);
        btnDressUpPage = bind(R.id.btn_dress_up_page);
        tvStatus = bind(R.id.tv_status);
        tvAvatarStatus = bind(R.id.tv_avatar_status);
        tvOutingStatus = bind(R.id.tv_outing_status);
        tvRoleFoldToggle = bind(R.id.tv_role_card_toggle);
        tvImageFoldToggle = bind(R.id.tv_image_toggle);
        tvOutingFoldToggle = bind(R.id.tv_outing_toggle);
        tvApiFoldToggle = bind(R.id.tv_api_toggle);
        tvLogFoldToggle = bind(R.id.tv_log_toggle);
        layoutRoleHeader = bind(R.id.layout_role_card_header);
        layoutRoleContent = bind(R.id.layout_role_card_content);
        layoutImageHeader = bind(R.id.layout_image_header);
        layoutImageContent = bind(R.id.layout_image_content);
        layoutOutingHeader = bind(R.id.layout_outing_header);
        layoutOutingContent = bind(R.id.layout_outing_content);
        layoutApiHeader = bind(R.id.layout_api_header);
        layoutApiContent = bind(R.id.layout_api_content);
        layoutLogHeader = bind(R.id.layout_log_header);
        layoutLogContent = bind(R.id.layout_log_content);
        rvOutingBackgrounds = bind(R.id.rv_outing_backgrounds);

        rvOutingBackgrounds.setLayoutManager(new LinearLayoutManager(this));
        outingBackgroundAdapter = new OutingBackgroundAdapter(new ArrayList<>(), this::removeOutingBackground);
        rvOutingBackgrounds.setAdapter(outingBackgroundAdapter);

        layoutRoleHeader.setOnClickListener(v -> setSectionExpanded(SECTION_ROLE, !roleExpanded));
        layoutImageHeader.setOnClickListener(v -> setSectionExpanded(SECTION_IMAGE, !imageExpanded));
        layoutOutingHeader.setOnClickListener(v -> setSectionExpanded(SECTION_OUTING, !outingExpanded));
        layoutApiHeader.setOnClickListener(v -> setSectionExpanded(SECTION_API, !apiExpanded));
        layoutLogHeader.setOnClickListener(v -> setSectionExpanded(SECTION_LOG, !logExpanded));

        rgApiProvider.setOnCheckedChangeListener((group, checkedId) -> {
            if (!suppressProviderChange) {
                onProviderChanged(checkedId, true);
            }
        });

        btnCreatePetCard.setOnClickListener(v -> navigateTo(UiNavigator.toCreatePet(this)));
        btnManagePetCards.setOnClickListener(v -> navigateTo(UiNavigator.toPetList(this)));
        btnHelpPage.setOnClickListener(v -> navigateTo(UiNavigator.toHelp(this)));
        btnDressUpPage.setOnClickListener(v -> navigateTo(UiNavigator.toDressUp(this)));
        btnViewChatLogs.setOnClickListener(v -> navigateTo(UiNavigator.toChatLogs(this)));
        btnViewErrorLogs.setOnClickListener(v -> navigateTo(UiNavigator.toErrorLogs(this)));
        btnSave.setOnClickListener(v -> saveSettings());
        btnTest.setOnClickListener(v -> testConnection());
        btnReset.setOnClickListener(v -> resetToDefaults());
        btnApplyAvatar.setOnClickListener(v -> applyAvatarFromSettings());
        btnPickAvatarImage.setOnClickListener(v -> pickAvatarImage());
        btnPickAvatarFromAssets.setOnClickListener(v -> pickAvatarFromAssetsFolder());
        btnSelectDiffOutfit.setOnClickListener(v -> selectDiffOutfit());
        btnUploadAvatarDiff.setOnClickListener(v -> uploadAvatarDiff());
        btnPickOutingBackgroundImage.setOnClickListener(v -> pickOutingBackgroundImage());
        btnAddOutingBackground.setOnClickListener(v -> addOutingBackground());
    }

    private void selectDiffOutfit() {
        List<DressOutfitSnapshot> outfits = SPUtils.getList(this, Constants.KEY_DRESS_UP_ITEMS, DressOutfitSnapshot.class);
        if (outfits == null || outfits.isEmpty()) {
            showToast(getString(R.string.settings_diff_no_outfit));
            return;
        }
        CharSequence[] names = new CharSequence[outfits.size()];
        for (int i = 0; i < outfits.size(); i++) {
            DressOutfitSnapshot item = outfits.get(i);
            names[i] = item == null || TextUtils.isEmpty(item.name) ? "未命名服装" : item.name;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_diff_pick_outfit)
                .setItems(names, (dialog, which) -> {
                    DressOutfitSnapshot item = outfits.get(which);
                    if (item == null || TextUtils.isEmpty(item.id)) {
                        showToast(getString(R.string.settings_diff_no_outfit));
                        return;
                    }
                    selectedDiffOutfitId = item.id;
                    selectedDiffOutfitName = TextUtils.isEmpty(item.name) ? item.id : item.name;
                    AvatarDiffStore.ensurePlaceholders(this, selectedDiffOutfitId);
                    tvAvatarStatus.setText(getString(R.string.settings_diff_selected_outfit, selectedDiffOutfitName));
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void uploadAvatarDiff() {
        if (TextUtils.isEmpty(selectedDiffOutfitId)) {
            showSelectOutfitRequiredDialog();
            return;
        }
        String[] labels = AvatarDiffStore.supportedEmotionLabels();
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_diff_pick_emotion)
                .setItems(labels, (dialog, which) -> {
                    pendingDiffOutfitId = selectedDiffOutfitId;
                    pendingDiffEmotion = AvatarDiffStore.emotionTagFromIndex(which);
                    if (avatarDiffPickerLauncher != null) {
                        avatarDiffPickerLauncher.launch(new String[]{"image/*"});
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void onAvatarDiffImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        if (TextUtils.isEmpty(pendingDiffOutfitId) || TextUtils.isEmpty(pendingDiffEmotion)) {
            showSelectOutfitRequiredDialog();
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        boolean saved = AvatarDiffStore.saveDiffImage(this, pendingDiffOutfitId, pendingDiffEmotion, uri);
        if (saved) {
            tvAvatarStatus.setText(getString(
                    R.string.settings_diff_upload_success,
                    TextUtils.isEmpty(selectedDiffOutfitName) ? pendingDiffOutfitId : selectedDiffOutfitName,
                    pendingDiffEmotion
            ));
        } else {
            showToast(getString(R.string.settings_diff_upload_failed));
        }
    }

    private void showSelectOutfitRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_diff_need_outfit_title)
                .setMessage(R.string.settings_diff_need_outfit_message)
                .setPositiveButton(R.string.settings_diff_need_outfit_confirm, null)
                .show();
    }

    private void openRequestedSection() {
        String section = getIntent() == null ? "" : getIntent().getStringExtra(EXTRA_OPEN_SECTION);
        if (TextUtils.isEmpty(section)) {
            return;
        }
        setSectionExpanded(section, true);
    }

    private void setSectionExpanded(@NonNull String section, boolean expanded) {
        if (SECTION_ROLE.equals(section)) {
            roleExpanded = expanded;
            layoutRoleContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
            tvRoleFoldToggle.setText(expanded ? R.string.settings_collapse : R.string.settings_expand);
        } else if (SECTION_IMAGE.equals(section)) {
            imageExpanded = expanded;
            layoutImageContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
            tvImageFoldToggle.setText(expanded ? R.string.settings_collapse : R.string.settings_expand);
        } else if (SECTION_OUTING.equals(section)) {
            outingExpanded = expanded;
            layoutOutingContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
            tvOutingFoldToggle.setText(expanded ? R.string.settings_collapse : R.string.settings_expand);
        } else if (SECTION_API.equals(section)) {
            apiExpanded = expanded;
            layoutApiContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
            tvApiFoldToggle.setText(expanded ? R.string.settings_collapse : R.string.settings_expand);
        } else if (SECTION_LOG.equals(section)) {
            logExpanded = expanded;
            layoutLogContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
            tvLogFoldToggle.setText(expanded ? R.string.settings_collapse : R.string.settings_expand);
        }
    }

    private void onProviderChanged(int checkedId, boolean clearInputs) {
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
        } else {
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
    }

    private void loadSettings() {
        ApiSettingsStore.ApiSettings settings = settingsStore.load();
        int checkedId = resolveProviderRadioId(settings.provider);
        suppressProviderChange = true;
        try {
            rgApiProvider.check(checkedId);
            etApiUrl.setText(settings.apiUrl);
            etApiKey.setText(settings.apiKey);
            etModelName.setText(settings.modelName);
            etAvatarImageUrl.setText(settings.avatarImageUrl);
            etAvatarUploadEndpoint.setText(settings.avatarUploadUrl);
            etAvatarRemoveBgEndpoint.setText(settings.avatarRemoveBgUrl);
            cbAvatarAutoProcess.setChecked(settings.avatarAutoProcess);
            cbAvatarPreserveOriginal.setChecked(settings.avatarPreserveOriginal);
            onProviderChanged(checkedId, false);
            tvAvatarStatus.setText("图片设置已加载");
            refreshOutingBackgrounds();
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

    private void saveSettings() {
        try {
            String apiUrl = etApiUrl.getText().toString().trim();
            String apiKey = etApiKey.getText().toString().trim();
            String modelName = etModelName.getText().toString().trim();
            String avatarImageUrl = etAvatarImageUrl.getText().toString().trim();
            String avatarUploadUrl = etAvatarUploadEndpoint.getText().toString().trim();
            String avatarRemoveBgUrl = etAvatarRemoveBgEndpoint.getText().toString().trim();
            boolean autoProcess = cbAvatarAutoProcess.isChecked();
            boolean preserveOriginal = cbAvatarPreserveOriginal.isChecked();

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

            settingsStore.save(new ApiSettingsStore.ApiSettings(
                    provider,
                    apiUrl,
                    apiKey,
                    modelName,
                    avatarImageUrl,
                    avatarUploadUrl,
                    avatarRemoveBgUrl,
                        autoProcess,
                        preserveOriginal
            ));

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
            }

            tvStatus.setText(getString(R.string.settings_saved));
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
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

    private void testConnection() {
        String apiUrl = etApiUrl.getText().toString().trim();
        String apiKey = etApiKey.getText().toString().trim();
        String modelName = etModelName.getText().toString().trim();
        int selectedProvider = rgApiProvider.getCheckedRadioButtonId();
        String provider = resolveProviderValue(selectedProvider);

        apiUrl = resolveApiUrl(provider, apiUrl);
        modelName = resolveModelName(provider, modelName);

        if (Constants.PROVIDER_CUSTOM.equals(provider) && apiUrl.isEmpty()) {
            showToast("请先填入 API URL");
            return;
        }

        tvStatus.setText("⏳ 正在测试连接...");
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        btnTest.setEnabled(false);

        ApiConnectionTester.testConnection(provider, apiUrl, apiKey, modelName, new ApiConnectionTester.Callback() {
            @Override
            public void onSuccess(@NonNull String message) {
                runOnUiThread(() -> {
                    tvStatus.setText(message);
                    tvStatus.setTextColor(ContextCompat.getColor(SettingsActivity.this, android.R.color.holo_green_dark));
                    Toast.makeText(SettingsActivity.this, R.string.settings_test_ok, Toast.LENGTH_SHORT).show();
                    btnTest.setEnabled(true);
                });
            }

            @Override
            public void onFailure(@NonNull String message) {
                runOnUiThread(() -> {
                    tvStatus.setText("✗ " + message);
                    tvStatus.setTextColor(ContextCompat.getColor(SettingsActivity.this, android.R.color.holo_red_dark));
                    Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                    btnTest.setEnabled(true);
                });
            }
        });
    }

    private void resetToDefaults() {
        rgApiProvider.check(R.id.rb_openai);
        etApiUrl.setText(Constants.OPENAI_BASE_URL);
        etApiKey.setText("");
        etModelName.setText(Constants.OPENAI_DEFAULT_MODEL);
        etAvatarImageUrl.setText("");
        etAvatarUploadEndpoint.setText("");
        etAvatarRemoveBgEndpoint.setText("");
        cbAvatarAutoProcess.setChecked(false);
        cbAvatarPreserveOriginal.setChecked(false);
        tvStatus.setText("✓ 已重置为默认设置");
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
    }

    private void applyAvatarFromSettings() {
        Pet activePet = resolveActivePet();
        if (activePet == null) {
            showToast(getString(R.string.settings_avatar_missing_role));
            return;
        }

        String imageUrl = etAvatarImageUrl.getText().toString().trim();
        if (TextUtils.isEmpty(imageUrl)) {
            showToast(getString(R.string.settings_avatar_missing_url));
            return;
        }

        tvAvatarStatus.setText(getString(R.string.settings_avatar_processing));
        btnApplyAvatar.setEnabled(false);

        String uploadEndpoint = etAvatarUploadEndpoint.getText().toString().trim();
        String removeBgEndpoint = etAvatarRemoveBgEndpoint.getText().toString().trim();
        boolean autoProcess = cbAvatarAutoProcess.isChecked();
        boolean preserveOriginal = cbAvatarPreserveOriginal.isChecked();

        AvatarImagePipeline.processRemoteImage(
                this,
                imageUrl,
                uploadEndpoint,
                removeBgEndpoint,
                autoProcess,
                preserveOriginal,
                activePet.getId(),
                new AvatarImagePipeline.Callback() {
                    @Override
                    public void onSuccess(@NonNull String localAvatarUri) {
                        runOnUiThread(() -> {
                            activePet.setAvatar(localAvatarUri);
                            petStore.updatePet(activePet);
                            tvAvatarStatus.setText(getString(R.string.settings_avatar_apply_success));
                            btnApplyAvatar.setEnabled(true);
                            Toast.makeText(SettingsActivity.this, R.string.settings_avatar_apply_success, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(@NonNull String message) {
                        runOnUiThread(() -> {
                            tvAvatarStatus.setText(getString(R.string.settings_avatar_failure, message));
                            btnApplyAvatar.setEnabled(true);
                            showError(message);
                        });
                    }
                }
        );
    }

    private void refreshOutingBackgrounds() {
        List<OutingBackgroundStore.Entry> entries = outingBackgroundStore.loadAll();
        outingBackgroundAdapter.submit(entries);
        updateOutingBackgroundStatus(entries.size());
    }

    private void addOutingBackground() {
        String placeName = etOutingBgPlaceName.getText().toString().trim();
        String environment = etOutingBgEnvironment.getText().toString().trim();
        String imageUrl = etOutingBgImageUrl.getText().toString().trim();
        String deltaText = etOutingBgDelta.getText().toString().trim();

        if (TextUtils.isEmpty(placeName)) {
            showToast("请填写外出地点名称");
            return;
        }
        if (TextUtils.isEmpty(environment)) {
            showToast("请填写环境描述");
            return;
        }

        int affectionDelta = 4;
        if (!TextUtils.isEmpty(deltaText)) {
            try {
                affectionDelta = Integer.parseInt(deltaText);
            } catch (NumberFormatException e) {
                showToast("好感增量请输入整数");
                return;
            }
        }

        OutingBackgroundStore.Entry entry = new OutingBackgroundStore.Entry(
                System.currentTimeMillis(),
                placeName,
                environment,
                imageUrl,
                affectionDelta
        );
        outingBackgroundStore.add(entry);
        etOutingBgPlaceName.setText("");
        etOutingBgEnvironment.setText("");
        etOutingBgImageUrl.setText("");
        etOutingBgDelta.setText("");
        refreshOutingBackgrounds();
        showToast("外出背景已添加");
    }

    private void removeOutingBackground(long id) {
        if (outingBackgroundStore.removeById(id)) {
            refreshOutingBackgrounds();
            showToast("外出背景已删除");
        }
    }

    private void updateOutingBackgroundStatus(int count) {
        if (count <= 0) {
            tvOutingStatus.setText(R.string.settings_outing_bg_empty);
            return;
        }
        tvOutingStatus.setText(getString(R.string.settings_outing_bg_count_format, count));
    }

    private void pickOutingBackgroundImage() {
        if (outingBackgroundPickerLauncher == null) {
            showToast("图片选择器未初始化");
            return;
        }
        outingBackgroundPickerLauncher.launch(new String[]{"image/*"});
    }

    private void pickAvatarImage() {
        if (avatarPickerLauncher == null) {
            showToast("角色图选择器未初始化");
            return;
        }
        avatarPickerLauncher.launch(new String[]{"image/*"});
    }

    private void onAvatarImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        etAvatarImageUrl.setText(uri.toString());
        tvAvatarStatus.setText(getString(R.string.settings_avatar_local_import_ok));
    }

    private void pickAvatarFromAssetsFolder() {
        try {
            String[] files = getAssets().list("character");
            if (files == null || files.length == 0) {
                showToast(getString(R.string.settings_avatar_asset_empty));
                return;
            }
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_avatar_asset_pick_title)
                .setItems(files, (dialog, which) -> {
                    String selected = files[which];
                    String source = "asset:character/" + selected;
                    etAvatarImageUrl.setText(source);
                    tvAvatarStatus.setText(getString(R.string.settings_avatar_asset_selected, selected));
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
        } catch (Exception e) {
            Log.w(TAG, "读取应用内角色素材失败: " + e.getMessage(), e);
            showToast(getString(R.string.settings_avatar_asset_empty));
        }
    }

    private void onOutingBackgroundPicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        etOutingBgImageUrl.setText(uri.toString());
        tvOutingStatus.setText(R.string.settings_outing_bg_import_hint);
    }

    @Nullable
    private Pet resolveActivePet() {
        List<Pet> pets = petStore.getAllPets();
        if (pets.isEmpty()) {
            return null;
        }
        long savedId = 0L;
        try {
            savedId = Long.parseLong(SPUtils.getString(this, Constants.KEY_ACTIVE_PET_ID, "0"));
        } catch (Exception ignored) {
        }
        if (savedId > 0) {
            Pet pet = petStore.getPetById(savedId);
            if (pet != null) {
                return pet;
            }
        }
        return pets.get(0);
    }

    private void showError(String message) {
        tvStatus.setText("✗ " + message);
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.w(TAG, message);
    }

    private static class OutingBackgroundAdapter extends RecyclerView.Adapter<OutingBackgroundAdapter.BackgroundViewHolder> {

        private final List<OutingBackgroundStore.Entry> items;
        private final OnBackgroundRemoveListener removeListener;

        OutingBackgroundAdapter(List<OutingBackgroundStore.Entry> items, OnBackgroundRemoveListener removeListener) {
            this.items = items;
            this.removeListener = removeListener;
        }

        void submit(List<OutingBackgroundStore.Entry> newItems) {
            items.clear();
            if (newItems != null) {
                items.addAll(newItems);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BackgroundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outing_background_manage, parent, false);
            return new BackgroundViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BackgroundViewHolder holder, int position) {
            OutingBackgroundStore.Entry item = items.get(position);
            holder.tvName.setText(item.placeName);
            holder.tvEnvironment.setText(item.environment);
            holder.tvImageUrl.setText(TextUtils.isEmpty(item.imageUrl) ? "未填写图片地址" : item.imageUrl);
            holder.tvDelta.setText(holder.itemView.getContext().getString(R.string.affection_plus_format, item.affectionDelta));
            holder.btnRemove.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove(item.id);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class BackgroundViewHolder extends RecyclerView.ViewHolder {
            final TextView tvName;
            final TextView tvEnvironment;
            final TextView tvImageUrl;
            final TextView tvDelta;
            final Button btnRemove;

            BackgroundViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_outing_manage_name);
                tvEnvironment = itemView.findViewById(R.id.tv_outing_manage_environment);
                tvImageUrl = itemView.findViewById(R.id.tv_outing_manage_image_url);
                tvDelta = itemView.findViewById(R.id.tv_outing_manage_delta);
                btnRemove = itemView.findViewById(R.id.btn_outing_manage_remove);
            }
        }

        interface OnBackgroundRemoveListener {
            void onRemove(long id);
        }
    }

    private static class DressOutfitSnapshot {
        public String id;
        public String name;
    }
}
