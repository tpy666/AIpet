package com.example.aipet.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipet.R;
import com.example.aipet.data.model.Message;
import com.example.aipet.data.model.Pet;
import com.example.aipet.network.ApiClient;
import com.example.aipet.network.ApiConfig;
import com.example.aipet.network.AssistantReplyFormatter;
import com.example.aipet.network.ChatRequest;
import com.example.aipet.network.NetworkConfigBootstrap;
import com.example.aipet.network.PetPromptBuilder;
import com.example.aipet.ui.adapter.ChatAdapter;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.ChatLogger;
import com.example.aipet.util.Constants;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.PetStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天页面：RecyclerView + 网络 API 调用（第二阶段升级版）
 * 
 * 功能：
 *   • 集成真实 API 调用
 *   • 显示加载状态
 *   • 网络异常错误处理
 *   • 离线/API 故障时自动降级到本地模拟
 */
public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";
    private static final String EXTRA_PET = UiNavigator.EXTRA_PET;

    // ========== UI 组件 ==========
    private RecyclerView rvChat;
    private Spinner spChatPetSelector;
    private TextView tvChatPetInfo;
    private TextView tvChatEmptyHint;
    private LinearLayout layoutInputBar;
    private EditText etMessageInput;
    private Button btnSend;
    
    // ========== 适配器和数据 ==========
    private ChatAdapter chatAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private final List<Pet> availablePets = new ArrayList<>();
    private ArrayAdapter<String> petSelectorAdapter;
    private Pet activePet;
    private boolean suppressPetSelection;
    
    // ========== 网络配置 ==========
    private ApiConfig apiConfig;
    private boolean useNetworkApi = true;  // 是否使用网络 API
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // ========== 日志记录 ==========
    private ChatLogger chatLogger;
    private PetStore petStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupScreen("聊天", true);
        // 进入聊天页时再次同步配置，确保从设置页返回或冷启动都使用最新 provider。
        NetworkConfigBootstrap.syncFromStorage(this);
        apiConfig = ApiConfig.getInstance();
        chatLogger = UtilHub.logger(this);
        petStore = UtilHub.petStore(this);
        initViews();
        loadPet();
        
        // 检查 API 配置
        checkApiConfiguration();
    }

    private void initViews() {
        tvChatPetInfo = bind(R.id.tv_chat_pet_info);
        tvChatEmptyHint = bind(R.id.tv_chat_empty_hint);
        spChatPetSelector = bind(R.id.sp_chat_pet_selector);
        layoutInputBar = bind(R.id.layout_input_bar);
        rvChat = bind(R.id.rv_chat_messages);
        etMessageInput = bind(R.id.et_message_input);
        btnSend = bind(R.id.btn_send);

        chatAdapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadPet() {
        Pet selectedPet = null;
        if (getIntent() != null && getIntent().hasExtra(EXTRA_PET)) {
            selectedPet = (Pet) getIntent().getSerializableExtra(EXTRA_PET, Pet.class);
        }

        availablePets.clear();
        availablePets.addAll(petStore.getAllPets());

        setupPetSelector();

        if (selectedPet == null && !availablePets.isEmpty()) {
            selectedPet = availablePets.get(0);
        }

        activePet = selectedPet;
        if (activePet == null) {
            tvChatPetInfo.setText("当前没有可聊天的角色");
            tvChatEmptyHint.setVisibility(View.VISIBLE);
            tvChatEmptyHint.setText("请先创建一个角色，再开始聊天。");
            spChatPetSelector.setVisibility(View.GONE);
            rvChat.setVisibility(View.GONE);
            layoutInputBar.setVisibility(View.GONE);
            Toast.makeText(this, "请先创建宠物角色，然后再聊天", Toast.LENGTH_LONG).show();
            return;
        }

        spChatPetSelector.setVisibility(View.VISIBLE);
        tvChatEmptyHint.setVisibility(View.GONE);
        rvChat.setVisibility(View.VISIBLE);
        layoutInputBar.setVisibility(View.VISIBLE);
        selectPetById(activePet.getId(), false);
    }

    private void setupPetSelector() {
        List<String> petLabels = new ArrayList<>();
        for (Pet pet : availablePets) {
            petLabels.add(buildPetLabel(pet));
        }

        petSelectorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petLabels);
        petSelectorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChatPetSelector.setAdapter(petSelectorAdapter);
        spChatPetSelector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (suppressPetSelection || position < 0 || position >= availablePets.size()) {
                    return;
                }
                selectPetById(availablePets.get(position).getId(), true);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private String buildPetLabel(Pet pet) {
        return pet.getName() + " · " + pet.getSpecies();
    }

    private void selectPetById(long petId, boolean resetConversation) {
        for (int i = 0; i < availablePets.size(); i++) {
            Pet pet = availablePets.get(i);
            if (pet.getId() == petId) {
                activePet = pet;
                suppressPetSelection = true;
                spChatPetSelector.setSelection(i, false);
                suppressPetSelection = false;
                updateActivePetPanel(resetConversation);
                return;
            }
        }
    }

    private void updateActivePetPanel(boolean resetConversation) {
        if (activePet == null) {
            return;
        }

        tvChatPetInfo.setText(String.format("当前角色：%s · %s · %s",
                activePet.getName(), activePet.getSpecies(), activePet.getPersonality()));

        if (resetConversation) {
            messageList.clear();
            chatAdapter.notifyDataSetChanged();
            String welcome = String.format("你好，我是%s，%s的%s，陪你说说心里话吧～", activePet.getName(), activePet.getSpecies(), activePet.getPersonality());
            appendMessage(new Message(Message.ROLE_ASSISTANT, welcome, activePet.getId()));
        }
    }

    private void sendMessage() {
        if (activePet == null) {
            Toast.makeText(this, "当前未绑定角色，请先创建宠物", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "请输入消息内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 记录用户消息
        chatLogger.logUserMessage(text, activePet.getName());

        // 添加用户消息
        Message userMsg = new Message(Message.ROLE_USER, text, activePet.getId());
        appendMessage(userMsg);
        etMessageInput.setText("");
        
        // 禁用发送按钮，显示加载状态
        btnSend.setEnabled(false);
        btnSend.setText("加载中...");
        
        // 根据配置选择使用 API 还是本地模拟
        if (useNetworkApi) {
            requestAIReplyFromApi(text);
        } else {
            requestAIReplyLocal(text);
        }
    }

    private void appendMessage(Message message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }
    
    // ========== 网络配置检查 ==========
    
    /**
     * 检查 API 配置是否有效
     */
    private void checkApiConfiguration() {
        // 记录配置检查日志
        Log.d(TAG, "========== 检查 API 配置 ==========");
        Log.d(TAG, "当前提供商: " + apiConfig.getProvider());
        Log.d(TAG, "配置是否有效: " + apiConfig.isConfigValid());
        Log.d(TAG, "配置详情: " + apiConfig.getConfigSummary());
        
        if (!apiConfig.isConfigValid()) {
            String errorMsg = "API 配置无效: " + apiConfig.getValidationError();
            logWarning(errorMsg);
            logWarning("将使用本地模拟模式");
            useNetworkApi = false;
            Log.e(TAG, errorMsg);
            
            // 显示配置提示
            showApiConfigurationHint();
        } else {
            useNetworkApi = true;
            String successMsg = "API 配置有效，使用网络模式";
            logDebug(successMsg);
            logDebug(apiConfig.getConfigSummary());
            Log.i(TAG, successMsg);
            Log.i(TAG, "API URL: " + apiConfig.getApiUrl());
            Log.i(TAG, "API 模型: " + apiConfig.getApiModel());
        }
        Log.d(TAG, "useNetworkApi = " + useNetworkApi);
        Log.d(TAG, "================================");
    }
    
    /**
     * 显示 API 配置提示
     */
    private void showApiConfigurationHint() {
        String hint = "【开发提示】API 未配置，使用本地模拟回复。\n" +
                     "要启用真实 AI 功能，请配置：\n" +
                     "ApiClient.configureOpenAI(\"your-api-key\", \"gpt-3.5-turbo\")";
        
        mainHandler.post(() -> {
            Toast.makeText(ChatActivity.this, hint, Toast.LENGTH_LONG).show();
        });
    }
    
    // ========== 网络 API 调用 ==========
    
    /**
     * 从 API 请求 AI 回复
     */
    private void requestAIReplyFromApi(String userMessage) {
        // 构造宠物信息
        ChatRequest.PetInfo petInfo = new ChatRequest.PetInfo(activePet);
        
        // 生成宠物系统提示词
        String systemPrompt = PetPromptBuilder.buildSystemPrompt(activePet);
        
        String apiUrl = ApiClient.getApiConfig().getApiUrl();
        chatLogger.logApiRequest(apiUrl, Constants.HTTP_METHOD_POST, "chat_message");

        ApiClient.sendChatMessage(userMessage, activePet.getId(), petInfo, systemPrompt, new ApiClient.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                mainHandler.post(() -> {
                    AssistantReplyFormatter.FormatResult formatted = AssistantReplyFormatter.format(reply);
                    chatLogger.logApiResponse(formatted.displayText, 200);
                    chatLogger.logPetReply(
                            !formatted.answer.isEmpty() ? formatted.answer : formatted.displayText,
                            activePet.getName()
                    );
                    handleApiReply(formatted.displayText);
                    resetSendButton();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                mainHandler.post(() -> {
                    chatLogger.logApiError(errorMessage, "NETWORK_ERROR", errorMessage);
                    chatLogger.logFallbackUsage("API 请求失败，使用本地模拟");
                    Toast.makeText(ChatActivity.this, 
                            "API 请求失败: " + errorMessage + "\n将使用本地回复", 
                            Toast.LENGTH_SHORT).show();
                    
                    // 降级到本地模拟
                    String localReply = generatePetReply(userMessage);
                    chatLogger.logPetReply(localReply, activePet.getName());
                    handleApiReply(localReply);
                    resetSendButton();
                });
            }
        });
    }
    
    /**
     * 本地模拟 AI 回复
     */
    private void requestAIReplyLocal(String userMessage) {
        mainHandler.postDelayed(() -> {
            logDebug("使用本地模拟回复");
            String reply = generatePetReply(userMessage);
            handleApiReply(reply);
            resetSendButton();
        }, 500);
    }
    
    /**
     * 处理 AI 回复（来自 API 或本地）
     */
    private void handleApiReply(String reply) {
        Message petMsg = new Message(Message.ROLE_ASSISTANT, reply, activePet.getId());
        appendMessage(petMsg);
    }
    
    /**
     * 重置发送按钮状态
     */
    private void resetSendButton() {
        btnSend.setEnabled(true);
        btnSend.setText("发送");
    }

    private String generatePetReply(String userInput) {
        if (activePet == null) {
            return "我现在还没有名字，你可以先创建一个宠物哦~";
        }

        String base = "";
        String name = activePet.getName();
        String species = activePet.getSpecies();
        String personality = activePet.getPersonality();
        String style = activePet.getSpeakingStyle();

        switch (style) {
            case "卖萌":
                base = String.format("%s眨眨眼说：", name);
                break;
            case "暖心":
                base = String.format("%s温柔地说：", name);
                break;
            case "幽默":
                base = String.format("%s笑着回应：", name);
                break;
            case "文艺":
                base = String.format("%s轻声吟道：", name);
                break;
            case "直白":
                base = String.format("%s直接说道：", name);
                break;
            default:
                base = String.format("%s回应：", name);
                break;
        }

        String mood = "";
        if (personality.contains("温柔") || personality.contains("稳重")) {
            mood = "我会一直在你身边，陪你度过每一个不安的夜晚。";
        } else if (personality.contains("活泼") || personality.contains("撒娇")) {
            mood = "今天也要和你一起做开心事情哦~";
        } else if (personality.contains("高冷")) {
            mood = "别担心，我的存在就是你的依赖。";
        } else {
            mood = "不管发生什么，我都会陪在你左右。";
        }

        String content = userInput.trim();
        if (content.length() > 20) {
            content = content.substring(0, 20) + "...";
        }

        return String.format("%s \n你说‘%s’，%s \n可爱外观：%s。", base, content, mood, activePet.getAppearance());
    }    
    // ========== 日志工具方法 ==========
    
    private void logDebug(String message) {
        if (apiConfig.isDebugLogging()) {
            android.util.Log.d(TAG, message);
        }
    }
    
    private void logWarning(String message) {
        android.util.Log.w(TAG, message);
    }}
