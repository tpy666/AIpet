package com.example.aipet.ui.activity;

import android.Manifest;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;
import com.example.aipet.network.ApiClient;
import com.example.aipet.network.AssistantReplyFormatter;
import com.example.aipet.network.ChatRequest;
import com.example.aipet.network.NetworkConfigBootstrap;
import com.example.aipet.network.PetPromptBuilder;
import com.example.aipet.ui.avatar.AvatarUploadPort;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.ChatLogger;
import com.example.aipet.util.Constants;
import com.example.aipet.util.SPUtils;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.store.PetStore;

import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 首页 - 主菜单页面
 * 包含导航至各个功能页面的按钮
 */
public class MainActivity extends BaseActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 101;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA);
    private static final long WEATHER_REFRESH_INTERVAL_MS = 20 * 60 * 1000L;
    private static final int RECENT_CHAT_LIMIT = 20;
    private static final int TOP_VOICE_BUBBLE_LIMIT = 5;
    private static final long TOP_BUBBLE_REMOVE_INTERVAL_MS = 80L;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final List<RecentChatItem> recentChats = new ArrayList<>();
    private final int[] stageBackgrounds = {
            R.drawable.bg_screen_atmosphere,
            R.drawable.bg_home_outing
    };
    private final List<OutfitItem> outfitCatalog = new ArrayList<>();
    private final OkHttpClient weatherHttpClient = new OkHttpClient();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private View rootLayout;
    private LinearLayout recentPanel;
    private LinearLayout topReplyBubbleContainer;
    private LinearLayout petSwitchListLayout;
    private FrameLayout recentFullscreenLayout;
    private LinearLayout recentBubblesLayout;
    private LinearLayout recentBubblesFullscreenLayout;
    private ScrollView recentScrollView;
    private ScrollView recentFullscreenScrollView;
    private TextView tvTime;
    private TextView tvMeta;
    private TextView tvCurrentPetChip;
    private TextView tvPetName;
    private TextView tvPetAvatarPlaceholder;
    private ImageView ivPetAvatar;
    private EditText etHomeInput;
    private ImageButton btnHomeSend;
    private ImageButton btnRecentFullscreen;
    private ImageButton btnRecentFullscreenClose;
    private ImageButton btnPetSwitchExpand;

    private ChatLogger chatLogger;
    private PetStore petStore;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private Pet activePet;
    private int affectionValue = 50;
    private int noticeCount = 0;
    private int stageBackgroundIndex = 0;
    private long lastWeatherUpdateAt = 0L;
    private String latestWeatherText = "天气加载中...";
    private String currentCityName = "定位中";
    private String currentWeekText = "";
    private String currentDateText = "";
    private boolean petSwitcherExpanded = false;
    private TextView initialNoticeBubble;
    private TextView initialAffectionBubble;
    private final AvatarUploadPort avatarUploadPort = new AvatarUploadPort() {
        @Override
        public void requestLocalImage(@NonNull ActivityResultLauncher<String> pickerLauncher) {
            pickerLauncher.launch("image/*");
        }

        @Override
        public void requestRemoteImage(@NonNull String imageUrl) {
            // 预留：后续可接入真实下载、缓存与鉴权链路。
        }
    };

    private final ActivityResultLauncher<String> localImagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onLocalImagePicked);

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateDateTimeOnly();
            tryRefreshWeather(false);
            clockHandler.postDelayed(this, 30_000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupScreen("AI 宠物助手", false);
        chatLogger = UtilHub.logger(this);
        petStore = UtilHub.petStore(this);
        NetworkConfigBootstrap.syncFromStorage(this);

        bindViews();
        initTextToSpeech();
        initOutfitCatalog();
        loadActivePet();
        initViews();
        updateDateTimeOnly();
        tryRefreshWeather(true);
        initTopDefaultBubbles();
        refreshPetSwitcher();
        updateNoticeBubble();
        updateAffectionBubble();
        clockHandler.post(clockRunnable);
    }

    private void bindViews() {
        rootLayout = bind(R.id.main_root);
        recentPanel = bind(R.id.layout_recent_chat_panel);
        topReplyBubbleContainer = bind(R.id.layout_top_reply_bubbles);
        petSwitchListLayout = bind(R.id.layout_pet_switch_list);
        tvTime = bind(R.id.tv_time);
        tvMeta = bind(R.id.tv_meta);
        tvCurrentPetChip = bind(R.id.tv_current_pet_chip);
        recentBubblesLayout = bind(R.id.layout_recent_bubbles);
        recentBubblesFullscreenLayout = bind(R.id.layout_recent_bubbles_full);
        recentScrollView = bind(R.id.sv_recent_chat);
        recentFullscreenScrollView = bind(R.id.sv_recent_chat_fullscreen);
        recentFullscreenLayout = bind(R.id.layout_recent_fullscreen);
        tvPetName = bind(R.id.tv_pet_name);
        ivPetAvatar = bind(R.id.iv_pet_avatar);
        tvPetAvatarPlaceholder = bind(R.id.tv_pet_avatar_placeholder);
        etHomeInput = bind(R.id.et_home_input);
        btnHomeSend = bind(R.id.btn_home_send);
        btnRecentFullscreen = bind(R.id.btn_recent_fullscreen);
        btnRecentFullscreenClose = bind(R.id.btn_recent_fullscreen_close);
        btnPetSwitchExpand = bind(R.id.btn_pet_switch_expand);
    }

    private void initViews() {
        click(R.id.btn_affection, v -> showAffectionDialog());
        click(R.id.btn_logs, v -> navigateTo(UiNavigator.toChatLogs(this)));
        click(R.id.btn_dress_up, v -> showDressUpDialog());

        click(R.id.nav_help, v -> showHelpDialog());
        click(R.id.nav_feed, v -> onFeedPet());
        click(R.id.nav_store, v -> onOpenStore());
        click(R.id.nav_outing, v -> onOutingToggle());
        click(R.id.nav_settings, v -> navigateTo(UiNavigator.toSettings(this)));
        btnPetSwitchExpand.setOnClickListener(v -> togglePetSwitcher());
        ivPetAvatar.setOnClickListener(v -> showAvatarSourceDialog());

        click(R.id.btn_recent_toggle, v -> toggleRecentPanel());
        btnRecentFullscreen.setOnClickListener(v -> showRecentFullscreen());
        btnRecentFullscreenClose.setOnClickListener(v -> hideRecentFullscreen());
        recentFullscreenLayout.setOnClickListener(v -> hideRecentFullscreen());

        btnHomeSend.setOnClickListener(v -> sendChatMessage());

        etHomeInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            sendChatMessage();
            return true;
        });
    }

    private void updateDateTimeOnly() {
        LocalDateTime now = LocalDateTime.now();
        tvTime.setText(now.format(TIME_FORMATTER));

        currentWeekText = getWeekdayText(now.getDayOfWeek());
        currentDateText = now.format(DATE_FORMATTER);
        updateMetaText();
    }

    private void updateMetaText() {
        tvMeta.setText(String.format(Locale.CHINA, "%s  %s  %s  %s", currentWeekText, currentDateText, currentCityName, latestWeatherText));
    }

    private void tryRefreshWeather(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastWeatherUpdateAt < WEATHER_REFRESH_INTERVAL_MS) {
            return;
        }
        if (hasLocationPermission()) {
            requestLocationAndWeather();
        } else if (force) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
            currentCityName = "未授权定位";
            latestWeatherText = "请开启定位权限";
            updateMetaText();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationAndWeather() {
        LocationManager locationManager = getSystemService(LocationManager.class);
        if (locationManager == null) {
            currentCityName = "定位不可用";
            latestWeatherText = "天气不可用";
            updateMetaText();
            return;
        }

        boolean hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        Location gpsLocation = null;
        Location networkLocation = null;
        try {
            if (hasFine) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (hasFine || hasCoarse) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation == null) {
                    networkLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }
            }
        } catch (SecurityException securityException) {
            currentCityName = "定位权限受限";
            latestWeatherText = "请允许精确定位";
            updateMetaText();
            return;
        }

        Location chosen = chooseBestLocation(gpsLocation, networkLocation);
        if (chosen == null) {
            currentCityName = "定位中";
            latestWeatherText = "等待定位";
            updateMetaText();
            return;
        }

        lastWeatherUpdateAt = System.currentTimeMillis();
        double lat = chosen.getLatitude();
        double lon = chosen.getLongitude();
        resolveCityNameAsync(lat, lon);
        requestWeatherByLatLon(lat, lon);
    }

    private Location chooseBestLocation(Location gps, Location network) {
        if (gps == null) {
            return network;
        }
        if (network == null) {
            return gps;
        }
        return gps.getTime() >= network.getTime() ? gps : network;
    }

    @SuppressWarnings("deprecation")
    private void resolveCityNameAsync(double latitude, double longitude) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.CHINA);
                geocoder.getFromLocation(latitude, longitude, 1, addresses -> {
                    if (addresses == null || addresses.isEmpty()) {
                        return;
                    }
                    Address addr = addresses.get(0);
                    String city = addr.getLocality();
                    if (TextUtils.isEmpty(city)) {
                        city = addr.getSubAdminArea();
                    }
                    if (!TextUtils.isEmpty(city)) {
                        currentCityName = city;
                        updateMetaText();
                    }
                });
                return;
            } catch (Exception ignored) {
                // Fallback to legacy flow below when framework geocoder callback is unavailable.
            }
        }

        ioExecutor.execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.CHINA);
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    String city = addr.getLocality();
                    if (TextUtils.isEmpty(city)) {
                        city = addr.getSubAdminArea();
                    }
                    if (!TextUtils.isEmpty(city)) {
                        String finalCity = city;
                        runOnUiThread(() -> {
                            currentCityName = finalCity;
                            updateMetaText();
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void requestWeatherByLatLon(double latitude, double longitude) {
        String weatherUrl = String.format(
                Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,weather_code",
                latitude,
                longitude
        );
        Request request = new Request.Builder().url(weatherUrl).get().build();
        weatherHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    latestWeatherText = "天气获取失败";
                    updateMetaText();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response res = response) {
                    if (!res.isSuccessful() || res.body() == null) {
                        runOnUiThread(() -> {
                            latestWeatherText = "天气服务异常";
                            updateMetaText();
                        });
                        return;
                    }
                    String body = res.body().string();
                    JSONObject root = new JSONObject(body);
                    JSONObject current = root.optJSONObject("current");
                    if (current == null) {
                        runOnUiThread(() -> {
                            latestWeatherText = "暂无天气数据";
                            updateMetaText();
                        });
                        return;
                    }

                    double temperature = current.optDouble("temperature_2m", Double.NaN);
                    int code = current.optInt("weather_code", -1);
                    if (Double.isNaN(temperature)) {
                        runOnUiThread(() -> {
                            latestWeatherText = "暂无天气数据";
                            updateMetaText();
                        });
                        return;
                    }

                    String weatherText = mapWeatherCode(code);
                    String display = String.format(Locale.CHINA, "%.0f°C %s", temperature, weatherText);
                    runOnUiThread(() -> {
                        latestWeatherText = display;
                        updateMetaText();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        latestWeatherText = "天气解析失败";
                        updateMetaText();
                    });
                }
            }
        });
    }

    private String mapWeatherCode(int code) {
        switch (code) {
            case 0:
                return "晴";
            case 1:
            case 2:
                return "多云";
            case 3:
                return "阴";
            case 45:
            case 48:
                return "雾";
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
                return "毛毛雨";
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
                return "下雨";
            case 71:
            case 73:
            case 75:
            case 77:
                return "下雪";
            case 80:
            case 81:
            case 82:
                return "阵雨";
            case 95:
            case 96:
            case 99:
                return "雷雨";
            default:
                return "未知";
        }
    }

    private String getWeekdayText(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "周一";
            case TUESDAY:
                return "周二";
            case WEDNESDAY:
                return "周三";
            case THURSDAY:
                return "周四";
            case FRIDAY:
                return "周五";
            case SATURDAY:
                return "周六";
            case SUNDAY:
            default:
                return "周日";
        }
    }

    private void toggleRecentPanel() {
        if (recentPanel.getVisibility() == View.VISIBLE) {
            recentPanel.setVisibility(View.GONE);
            hideRecentFullscreen();
            return;
        }

        noticeCount = 0;
        updateNoticeBubble();
        updateRecentPanelContent();
        recentPanel.setVisibility(View.VISIBLE);
    }

    private void showRecentFullscreen() {
        if (recentPanel.getVisibility() != View.VISIBLE) {
            recentPanel.setVisibility(View.VISIBLE);
        }
        updateRecentPanelContent();
        recentFullscreenLayout.setVisibility(View.VISIBLE);
    }

    private void hideRecentFullscreen() {
        if (recentFullscreenLayout.getVisibility() == View.VISIBLE) {
            recentFullscreenLayout.setVisibility(View.GONE);
        }
    }

    private void sendChatMessage() {
        String input = etHomeInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            showToast("请输入聊天内容");
            return;
        }
        if (activePet == null) {
            showToast("请先创建宠物角色");
            navigateTo(UiNavigator.toCreatePet(this));
            return;
        }

        etHomeInput.setText("");
        appendRecentChat(RecentChatRole.USER, input);
        chatLogger.logUserMessage(input, activePet.getName());
        chatLogger.logApiRequest(ApiClient.getApiConfig().getApiUrl(), Constants.HTTP_METHOD_POST, "home_chat_message");

        setSendLoading(true);
        ChatRequest.PetInfo petInfo = new ChatRequest.PetInfo(activePet);
        String prompt = PetPromptBuilder.buildSystemPrompt(activePet);
        ApiClient.sendChatMessage(input, activePet.getId(), petInfo, prompt, new ApiClient.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    setSendLoading(false);
                    String finalReply = reply == null ? "" : reply.trim();
                    if (finalReply.isEmpty()) {
                        finalReply = "抱歉，我刚刚没有听清楚，再说一次吧。";
                    }
                    AssistantReplyFormatter.FormatResult formatted = AssistantReplyFormatter.format(finalReply);
                    String displayText = formatted.displayText == null || formatted.displayText.trim().isEmpty()
                            ? finalReply
                            : formatted.displayText;
                    appendRecentChat(RecentChatRole.PET, displayText);
                    addTopReplyBubble(displayText);
                    speakText(displayText);
                    chatLogger.logApiResponse(displayText, 200);
                    chatLogger.logPetReply(displayText, activePet.getName());

                    affectionValue = Math.min(100, affectionValue + 2);
                    noticeCount += 1;
                    updateAffectionBubble();
                    updateNoticeBubble();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> {
                    setSendLoading(false);
                    String msg = "网络聊天失败: " + errorMessage;
                    appendRecentChat(RecentChatRole.SYSTEM, msg);
                    chatLogger.logApiError(errorMessage, "HOME_CHAT_ERROR", errorMessage);
                    showToast(msg);
                });
            }
        });
    }

    private void setSendLoading(boolean loading) {
        btnHomeSend.setEnabled(!loading);
        btnHomeSend.setAlpha(loading ? 0.55f : 1f);
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS || textToSpeech == null) {
                ttsReady = false;
                return;
            }
            int result = textToSpeech.setLanguage(Locale.CHINA);
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED;
        });
    }

    private void addTopReplyBubble(@NonNull String message) {
        enqueueTopBubble(message, true, true);
    }

    private void initTopDefaultBubbles() {
        if (topReplyBubbleContainer == null) {
            return;
        }
        topReplyBubbleContainer.removeAllViews();
        initialNoticeBubble = enqueueTopBubble(
                getString(R.string.home_chat_notice_format, noticeCount),
                false,
                false
        );
        initialAffectionBubble = enqueueTopBubble(
                getString(R.string.home_affection_format, affectionValue, getAffectionLevelText()),
                false,
                false
        );
    }

    private TextView enqueueTopBubble(@NonNull String message, boolean speakOnClick, boolean animateIn) {
        if (topReplyBubbleContainer == null || TextUtils.isEmpty(message)) {
            return null;
        }

        TextView bubbleView = new TextView(this);
        bubbleView.setText(message);
        bubbleView.setTextColor(ContextCompat.getColor(this, R.color.home_bubble_text));
        bubbleView.setTextSize(13f);
        bubbleView.setBackgroundResource(R.drawable.bg_home_bubble);
        bubbleView.setContentDescription(getString(R.string.home_voice_bubble_desc));
        bubbleView.setMaxWidth(dpToPx(270));
        int horizontal = dpToPx(12);
        int vertical = dpToPx(8);
        bubbleView.setPadding(horizontal, vertical, horizontal, vertical);
        bubbleView.setOnClickListener(speakOnClick ? v -> speakText(message) : null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dpToPx(8);
        bubbleView.setLayoutParams(params);
        if (animateIn) {
            bubbleView.setAlpha(0f);
            bubbleView.setScaleX(0.92f);
            bubbleView.setScaleY(0.92f);
        }

        topReplyBubbleContainer.addView(bubbleView);
        if (animateIn) {
            bubbleView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220L)
                    .start();
        }

        trimTopReplyBubblesIfNeeded();
        return bubbleView;
    }

    private void trimTopReplyBubblesIfNeeded() {
        if (topReplyBubbleContainer == null) {
            return;
        }
        if (topReplyBubbleContainer.getChildCount() <= TOP_VOICE_BUBBLE_LIMIT) {
            return;
        }

        View oldest = topReplyBubbleContainer.getChildAt(0);
        oldest.animate()
                .alpha(0f)
                .translationY(-dpToPx(8))
                .setDuration(200L)
                .withEndAction(() -> {
                    if (topReplyBubbleContainer != null) {
                        topReplyBubbleContainer.removeView(oldest);
                        if (topReplyBubbleContainer.getChildCount() > TOP_VOICE_BUBBLE_LIMIT) {
                            clockHandler.postDelayed(this::trimTopReplyBubblesIfNeeded, TOP_BUBBLE_REMOVE_INTERVAL_MS);
                        }
                    }
                })
                .start();
    }

    private void speakText(@NonNull String text) {
        if (!ttsReady || textToSpeech == null) {
            showToast(getString(R.string.home_voice_engine_unready));
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "home_voice_" + System.currentTimeMillis());
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void appendRecentChat(@NonNull RecentChatRole role, @NonNull String message) {
        recentChats.add(new RecentChatItem(role, message));
        if (recentChats.size() > RECENT_CHAT_LIMIT) {
            recentChats.remove(0);
        }
        updateRecentPanelContent();
    }

    private void updateRecentPanelContent() {
        renderRecentChats(recentBubblesLayout);
        renderRecentChats(recentBubblesFullscreenLayout);
        scrollRecentToBottom(recentScrollView);
        scrollRecentToBottom(recentFullscreenScrollView);
    }

    private void renderRecentChats(@NonNull LinearLayout container) {
        container.removeAllViews();
        if (recentChats.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.home_recent_empty);
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            empty.setTextSize(13f);
            container.addView(empty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (RecentChatItem item : recentChats) {
            container.addView(createBubbleView(inflater, item));
        }
    }

    private View createBubbleView(@NonNull LayoutInflater inflater, @NonNull RecentChatItem item) {
        if (item.role == RecentChatRole.USER) {
            View userBubble = inflater.inflate(R.layout.item_message_user, recentBubblesLayout, false);
            TextView tvUser = userBubble.findViewById(R.id.tv_message_user);
            tvUser.setText(item.message);
            return userBubble;
        }

        View petBubble = inflater.inflate(R.layout.item_message_pet, recentBubblesLayout, false);
        TextView tvAnswer = petBubble.findViewById(R.id.tv_message_pet_answer);
        if (item.role == RecentChatRole.SYSTEM) {
            tvAnswer.setText(String.format(Locale.CHINA, "系统: %s", item.message));
        } else {
            tvAnswer.setText(item.message);
        }
        return petBubble;
    }

    private void scrollRecentToBottom(@NonNull ScrollView scrollView) {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void updateNoticeBubble() {
        if (initialNoticeBubble != null && initialNoticeBubble.getParent() != null) {
            initialNoticeBubble.setText(getString(R.string.home_chat_notice_format, noticeCount));
        }
    }

    private void updateAffectionBubble() {
        if (initialAffectionBubble != null && initialAffectionBubble.getParent() != null) {
            initialAffectionBubble.setText(getString(
                    R.string.home_affection_format,
                    affectionValue,
                    getAffectionLevelText()
            ));
        }
    }

    private String getAffectionLevelText() {
        return affectionValue >= 80 ? "亲密" : (affectionValue >= 60 ? "友好" : "熟悉");
    }

    private void togglePetSwitcher() {
        petSwitcherExpanded = !petSwitcherExpanded;
        refreshPetSwitcher();
    }

    private void refreshPetSwitcher() {
        List<Pet> pets = petStore.getAllPets();
        String chipText = activePet == null
                ? getString(R.string.home_current_pet_none)
                : getString(R.string.home_current_pet_format, activePet.getName());
        tvCurrentPetChip.setText(chipText);

        petSwitchListLayout.removeAllViews();
        petSwitchListLayout.setVisibility(petSwitcherExpanded ? View.VISIBLE : View.GONE);
        if (!petSwitcherExpanded || pets.isEmpty()) {
            return;
        }

        for (Pet pet : pets) {
            TextView option = new TextView(this);
            option.setText(pet.getName());
            option.setTextColor(ContextCompat.getColor(this, R.color.ui_title));
            option.setTextSize(12f);
            option.setBackgroundResource(R.drawable.bg_home_meta_chip);
            option.setContentDescription(getString(R.string.home_pet_switch_item_format, pet.getName()));
            int horizontal = dpToPx(10);
            int vertical = dpToPx(6);
            option.setPadding(horizontal, vertical, horizontal, vertical);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = dpToPx(6);
            option.setLayoutParams(params);
            option.setOnClickListener(v -> switchActivePet(pet));
            petSwitchListLayout.addView(option);
        }
    }

    private void switchActivePet(@NonNull Pet pet) {
        activePet = pet;
        SPUtils.putString(this, Constants.KEY_ACTIVE_PET_ID, String.valueOf(pet.getId()));
        applySavedOutfit();
        petSwitcherExpanded = false;
        refreshPetSwitcher();
    }

    private void showAffectionDialog() {
        String message = "可增加好感度的行为:\n"
                + "- 聊天互动 +2\n"
                + "- 喂食 +5\n"
                + "- 商店购买礼物 +3\n"
                + "- 外出散步 +4\n\n"
                + "当前好感度: " + affectionValue;
        new AlertDialog.Builder(this)
                .setTitle("好感度查询")
                .setMessage(message)
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showDressUpDialog() {
        if (activePet == null) {
            showToast("请先创建宠物角色");
            return;
        }
        List<OutfitItem> unlocked = getUnlockedOutfits(activePet.getId());
        String[] names = new String[unlocked.size()];
        int checked = 0;
        String currentOutfitId = parseOutfitId(activePet.getAvatar());
        for (int i = 0; i < unlocked.size(); i++) {
            names[i] = unlocked.get(i).name;
            if (unlocked.get(i).id.equals(currentOutfitId)) {
                checked = i;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("换装")
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    OutfitItem selected = unlocked.get(which);
                    applyOutfit(selected);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void onFeedPet() {
        affectionValue = Math.min(100, affectionValue + 5);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        appendRecentChat(RecentChatRole.SYSTEM, "你喂食了宠物，好感度 +5");
        showToast("喂食成功，好感度 +5");
    }

    private void onOpenStore() {
        if (activePet == null) {
            showToast("请先创建宠物角色");
            return;
        }
        String[] items = new String[outfitCatalog.size()];
        List<OutfitItem> unlocked = getUnlockedOutfits(activePet.getId());
        for (int i = 0; i < outfitCatalog.size(); i++) {
            OutfitItem item = outfitCatalog.get(i);
            boolean has = containsOutfit(unlocked, item.id);
            items[i] = has ? (item.name + " (已拥有)") : (item.name + " (" + item.price + "金币)");
        }

        new AlertDialog.Builder(this)
                .setTitle("商店-服饰")
                .setItems(items, (dialog, which) -> handleShopSelection(outfitCatalog.get(which)))
                .setNegativeButton("关闭", null)
                .show();
    }

    private void onOutingToggle() {
        stageBackgroundIndex = (stageBackgroundIndex + 1) % stageBackgrounds.length;
        rootLayout.setBackgroundResource(stageBackgrounds[stageBackgroundIndex]);
        affectionValue = Math.min(100, affectionValue + 4);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        appendRecentChat(RecentChatRole.SYSTEM, "外出散步完成，背景已切换，好感度 +4");
        showToast("已切换外出背景");
    }

    private void handleShopSelection(OutfitItem item) {
        List<OutfitItem> unlocked = getUnlockedOutfits(activePet.getId());
        if (containsOutfit(unlocked, item.id)) {
            applyOutfit(item);
            showToast("已切换为 " + item.name);
            return;
        }

        unlockOutfit(activePet.getId(), item.id);
        affectionValue = Math.min(100, affectionValue + 3);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        appendRecentChat(RecentChatRole.SYSTEM, "购买了服饰「" + item.name + "」，好感度 +3");
        applyOutfit(item);
        showToast("购买成功并已换装: " + item.name);
    }

    private void applyOutfit(@NonNull OutfitItem outfitItem) {
        ivPetAvatar.setImageResource(outfitItem.imageResId);
        tvPetAvatarPlaceholder.setVisibility(View.GONE);
        if (activePet != null) {
            activePet.setAvatar("outfit:" + outfitItem.id);
            activePet.setAppearance(outfitItem.appearanceDesc);
            petStore.updatePet(activePet);
            tvPetName.setText(String.format(Locale.CHINA, "宠物: %s（%s）", activePet.getName(), outfitItem.name));
        }
    }

    private void initOutfitCatalog() {
        outfitCatalog.clear();
        outfitCatalog.add(new OutfitItem("default", "日常围巾", "围巾造型", R.drawable.ic_launcher_foreground, 0));
        outfitCatalog.add(new OutfitItem("hoodie", "连帽卫衣", "卫衣造型", R.mipmap.ic_launcher_round, 120));
        outfitCatalog.add(new OutfitItem("formal", "小礼服", "礼服造型", R.mipmap.ic_launcher, 180));
    }

    private void loadActivePet() {
        List<Pet> pets = petStore.getAllPets();
        if (pets.isEmpty()) {
            activePet = null;
            tvPetName.setText("宠物: 未创建");
            return;
        }

        long savedId = parseLongSafely(SPUtils.getString(this, Constants.KEY_ACTIVE_PET_ID, ""));
        if (savedId > 0) {
            Pet saved = petStore.getPetById(savedId);
            if (saved != null) {
                activePet = saved;
            }
        }
        if (activePet == null) {
            activePet = pets.get(0);
        }

        SPUtils.putString(this, Constants.KEY_ACTIVE_PET_ID, String.valueOf(activePet.getId()));
        applySavedOutfit();
        refreshPetSwitcher();
    }

    private void applySavedOutfit() {
        if (activePet == null) {
            ivPetAvatar.setImageResource(R.drawable.ic_pet_avatar_placeholder);
            tvPetAvatarPlaceholder.setVisibility(View.VISIBLE);
            return;
        }

        String avatarValue = activePet.getAvatar();
        if (isCustomAvatar(avatarValue)) {
            applyCustomAvatar(avatarValue);
            tvPetName.setText(String.format(Locale.CHINA, "宠物: %s", activePet.getName()));
            return;
        }

        String outfitId = parseOutfitId(activePet.getAvatar());
        OutfitItem selected = findOutfitById(outfitId);
        if (selected == null) {
            selected = outfitCatalog.get(0);
            unlockOutfit(activePet.getId(), selected.id);
            activePet.setAvatar("outfit:" + selected.id);
            activePet.setAppearance(selected.appearanceDesc);
            petStore.updatePet(activePet);
        }

        ivPetAvatar.setImageResource(selected.imageResId);
        tvPetAvatarPlaceholder.setVisibility(View.GONE);
        tvPetName.setText(String.format(Locale.CHINA, "宠物: %s（%s）", activePet.getName(), selected.name));
    }

    private void showAvatarSourceDialog() {
        if (activePet == null) {
            showToast("请先创建宠物角色");
            return;
        }

        CharSequence[] options = {
                getString(R.string.home_pet_avatar_upload_local),
                getString(R.string.home_pet_avatar_upload_remote)
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.home_pet_avatar_upload_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        avatarUploadPort.requestLocalImage(localImagePickerLauncher);
                    } else {
                        showRemoteUrlInputDialog();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRemoteUrlInputDialog() {
        EditText input = new EditText(this);
        input.setHint(R.string.home_pet_avatar_remote_hint);

        new AlertDialog.Builder(this)
                .setTitle(R.string.home_pet_avatar_upload_remote)
                .setView(input)
                .setPositiveButton(R.string.home_pet_avatar_remote_apply, (dialog, which) -> {
                    String url = input.getText() == null ? "" : input.getText().toString().trim();
                    if (TextUtils.isEmpty(url)) {
                        showToast("图片链接不能为空");
                        return;
                    }
                    avatarUploadPort.requestRemoteImage(url);
                    persistAndApplyAvatar("remote:" + url);
                    showToast(getString(R.string.home_pet_avatar_upload_reserved));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void onLocalImagePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        persistAndApplyAvatar("local:" + uri.toString());
    }

    private void persistAndApplyAvatar(@NonNull String avatarValue) {
        if (activePet == null) {
            return;
        }
        activePet.setAvatar(avatarValue);
        petStore.updatePet(activePet);
        applyCustomAvatar(avatarValue);
        tvPetName.setText(String.format(Locale.CHINA, "宠物: %s", activePet.getName()));
    }

    private boolean isCustomAvatar(String avatarValue) {
        return !TextUtils.isEmpty(avatarValue)
                && (avatarValue.startsWith("local:") || avatarValue.startsWith("remote:"));
    }

    private void applyCustomAvatar(@NonNull String avatarValue) {
        if (avatarValue.startsWith("local:")) {
            String uriText = avatarValue.substring("local:".length());
            try {
                ivPetAvatar.setImageURI(Uri.parse(uriText));
            } catch (Exception e) {
                ivPetAvatar.setImageResource(R.drawable.ic_pet_avatar_placeholder);
            }
        } else {
            // 远程图片暂时使用占位图，保留 URL 供后续真实下载链路使用。
            ivPetAvatar.setImageResource(R.drawable.ic_pet_avatar_placeholder);
        }
        tvPetAvatarPlaceholder.setVisibility(View.GONE);
    }

    @NonNull
    private List<OutfitItem> getUnlockedOutfits(long petId) {
        String key = Constants.KEY_UNLOCKED_OUTFITS_PREFIX + petId;
        String raw = SPUtils.getString(this, key, "default");
        String[] ids = raw.split(",");
        List<OutfitItem> list = new ArrayList<>();
        for (String id : ids) {
            OutfitItem item = findOutfitById(id.trim());
            if (item != null && !containsOutfit(list, item.id)) {
                list.add(item);
            }
        }
        if (list.isEmpty()) {
            list.add(outfitCatalog.get(0));
        }
        return list;
    }

    private void unlockOutfit(long petId, @NonNull String outfitId) {
        List<OutfitItem> unlocked = getUnlockedOutfits(petId);
        if (containsOutfit(unlocked, outfitId)) {
            return;
        }
        unlocked.add(findOutfitById(outfitId));
        StringBuilder ids = new StringBuilder();
        for (OutfitItem item : unlocked) {
            if (item != null) {
                ids.append(item.id).append(",");
            }
        }
        String value = ids.length() > 0 ? ids.substring(0, ids.length() - 1) : "default";
        SPUtils.putString(this, Constants.KEY_UNLOCKED_OUTFITS_PREFIX + petId, value);
    }

    private boolean containsOutfit(@NonNull List<OutfitItem> list, @NonNull String id) {
        for (OutfitItem item : list) {
            if (item != null && id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }

    private OutfitItem findOutfitById(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        for (OutfitItem item : outfitCatalog) {
            if (id.equals(item.id)) {
                return item;
            }
        }
        return null;
    }

    private String parseOutfitId(String avatarValue) {
        if (TextUtils.isEmpty(avatarValue)) {
            return "default";
        }
        if (avatarValue.startsWith("outfit:")) {
            return avatarValue.substring("outfit:".length());
        }
        return "default";
    }

    private long parseLongSafely(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }

    private void showHelpDialog() {
        String msg = "页面说明:\n"
                + "1. 顶部显示时间、日期地点天气\n"
                + "2. 右侧可查询好感度/聊天日志/换装\n"
                + "3. 底部可执行喂食、商店、外出和设置\n"
                + "4. 输入框可直接和宠物聊天，左侧可展开最近聊天夹层";
        new AlertDialog.Builder(this)
                .setTitle("帮助")
                .setMessage(msg)
                .setPositiveButton("明白了", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCATION_PERMISSION) {
            return;
        }

        boolean granted = false;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }

        if (granted) {
            requestLocationAndWeather();
        } else {
            currentCityName = "未授权定位";
            latestWeatherText = "天气不可用";
            updateMetaText();
        }
    }

    @Override
    protected void onDestroy() {
        clockHandler.removeCallbacks(clockRunnable);
        ioExecutor.shutdownNow();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }

    private static class OutfitItem {
        final String id;
        final String name;
        final String appearanceDesc;
        final int imageResId;
        final int price;

        OutfitItem(String id, String name, String appearanceDesc, int imageResId, int price) {
            this.id = id;
            this.name = name;
            this.appearanceDesc = appearanceDesc;
            this.imageResId = imageResId;
            this.price = price;
        }
    }

    private enum RecentChatRole {
        USER,
        PET,
        SYSTEM
    }

    private static class RecentChatItem {
        final RecentChatRole role;
        final String message;

        RecentChatItem(@NonNull RecentChatRole role, @NonNull String message) {
            this.role = role;
            this.message = message;
        }
    }
}
