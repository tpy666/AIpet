package com.example.aipet.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aipet.R;
import com.example.aipet.data.model.Pet;
import com.example.aipet.network.ApiClient;
import com.example.aipet.network.APIAnswer;
import com.example.aipet.network.ChatRequest;
import com.example.aipet.network.NetworkConfigBootstrap;
import com.example.aipet.network.PetPromptBuilder;
import com.example.aipet.ui.avatar.AvatarDiffStore;
import com.example.aipet.ui.avatar.AvatarExpressionResolver;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.ChatLogger;
import com.example.aipet.util.Constants;
import com.example.aipet.util.SPUtils;
import com.example.aipet.util.SceneImageLoader;
import com.example.aipet.util.UtilHub;
import com.example.aipet.util.AffectionHistoryStore;
import com.example.aipet.util.store.PetStore;

import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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

    private static final String TAG = "MainActivity";
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
            R.drawable.outside_city_park,
            R.drawable.outside_seaside_boardwalk,
            R.drawable.outside_pet_district
    };
    private final List<OutfitItem> outfitCatalog = new ArrayList<>();
    private final OkHttpClient weatherHttpClient = new OkHttpClient();
        private final OkHttpClient outingBackgroundHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private View rootLayout;
    private ImageView ivMainBackground;
    private ImageView ivStageBackground;
    private LinearLayout recentPanel;
    private LinearLayout topReplyBubbleContainer;
    private LinearLayout petSwitchListLayout;
    private FrameLayout recentFullscreenLayout;
    private LinearLayout recentBubblesLayout;
    private LinearLayout recentBubblesFullscreenLayout;
    private ScrollView recentScrollView;
    private ScrollView recentFullscreenScrollView;
    private TextView tvTime;
    private TextView tvDate;
    private TextView tvMeta;
    private TextView tvCurrentPetChip;
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
    private int stageBackgroundIndex = -1;
    private long lastWeatherUpdateAt = 0L;
    private String latestWeatherText = "天气加载中...";
    private String currentCityName = "定位中";
    private String currentWeekText = "";
    private String currentDateText = "";
    private boolean petSwitcherExpanded = false;
    private TextView initialNoticeBubble;
    private TextView initialAffectionBubble;
        private final ActivityResultLauncher<Intent> feedLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onFeedResult);

        private final ActivityResultLauncher<Intent> storeLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onStoreResult);

        private final ActivityResultLauncher<Intent> outingLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onOutingResult);

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
        applyFallbackOutingBackground();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadActivePet();
    }

    private void bindViews() {
        rootLayout = bind(R.id.main_root);
        ivMainBackground = bind(R.id.iv_main_background);
        ivStageBackground = bind(R.id.layout_content_slot);
        recentPanel = bind(R.id.layout_recent_chat_panel);
        topReplyBubbleContainer = bind(R.id.layout_top_reply_bubbles);
        petSwitchListLayout = bind(R.id.layout_pet_switch_list);
        tvTime = bind(R.id.tv_time);
        tvDate = bind(R.id.tv_date);
        tvMeta = bind(R.id.tv_meta);
        tvCurrentPetChip = bind(R.id.tv_current_pet_chip);
        recentBubblesLayout = bind(R.id.layout_recent_bubbles);
        recentBubblesFullscreenLayout = bind(R.id.layout_recent_bubbles_full);
        recentScrollView = bind(R.id.sv_recent_chat);
        recentFullscreenScrollView = bind(R.id.sv_recent_chat_fullscreen);
        recentFullscreenLayout = bind(R.id.layout_recent_fullscreen);
        ivPetAvatar = bind(R.id.iv_pet_avatar);
        tvPetAvatarPlaceholder = bind(R.id.tv_pet_avatar_placeholder);
        etHomeInput = bind(R.id.et_home_input);
        btnHomeSend = bind(R.id.btn_home_send);
        btnRecentFullscreen = bind(R.id.btn_recent_fullscreen);
        btnRecentFullscreenClose = bind(R.id.btn_recent_fullscreen_close);
        btnPetSwitchExpand = bind(R.id.btn_pet_switch_expand);
    }

    private void initViews() {
        click(R.id.btn_affection, v -> showAffectionPage());
        click(R.id.btn_logs, v -> navigateTo(UiNavigator.toChatLogs(this)));
        click(R.id.btn_dress_up, v -> navigateTo(UiNavigator.toDressUp(this)));

        click(R.id.nav_help, v -> navigateTo(UiNavigator.toHelp(this)));
        click(R.id.nav_feed, v -> onFeedPet());
        click(R.id.nav_store, v -> onOpenStore());
        click(R.id.nav_outing, v -> onOutingToggle());
        click(R.id.nav_settings, v -> navigateTo(UiNavigator.toSettings(this)));
        btnPetSwitchExpand.setOnClickListener(v -> togglePetSwitcher());

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
        tvDate.setText(now.format(DATE_FORMATTER));

        currentWeekText = getWeekdayText(now.getDayOfWeek());
        currentDateText = now.format(DATE_FORMATTER);
        updateMetaText();
    }

    private void updateMetaText() {
        tvMeta.setText(String.format(Locale.CHINA, "%s  %s", currentCityName, latestWeatherText));
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
            showToast(getString(R.string.chat_no_role_bound));
            return;
        }

        appendRecentChat(RecentChatRole.USER, input);
        chatLogger.logUserMessage(input, activePet.getName());
        etHomeInput.setText("");
        setSendLoading(true);

        ChatRequest.PetInfo petInfo = new ChatRequest.PetInfo(activePet);
        String systemPrompt = PetPromptBuilder.buildSystemPrompt(activePet);
        String apiUrl = ApiClient.getApiConfig().getApiUrl();
        chatLogger.logApiRequest(apiUrl, Constants.HTTP_METHOD_POST, "home_chat_message");

        ApiClient.sendChatMessage(input, activePet.getId(), petInfo, systemPrompt, new ApiClient.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    setSendLoading(false);

                    APIAnswer apiAnswer = APIAnswer.fromRaw(reply == null ? "" : reply);
                    String answerOnly = apiAnswer.answerOnly();

                    appendRecentChat(RecentChatRole.PET, answerOnly);
                    updateAvatarExpressionByReply(answerOnly);
                    addTopReplyBubble(answerOnly);
                    speakText(answerOnly);

                    chatLogger.logApiResponse(answerOnly, 200);
                    chatLogger.logApiAnswer(apiAnswer.toLogBlock());
                    chatLogger.logPetReply(answerOnly, activePet.getName());

                    affectionValue = Math.min(100, affectionValue + 2);
                    AffectionHistoryStore.append(MainActivity.this, "聊天互动", 2);
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
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
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

    private void showAffectionPage() {
        Intent intent = UiNavigator.toAffection(this);
        intent.putExtra("affection_value", affectionValue);
        navigateTo(intent);
    }

    private void onFeedPet() {
        feedLauncher.launch(UiNavigator.toFeed(this));
    }

    private void onOpenStore() {
        if (activePet == null) {
            showToast(getString(R.string.chat_no_role_bound));
            return;
        }
        storeLauncher.launch(UiNavigator.toStore(this));
    }

    private void onOutingToggle() {
        outingLauncher.launch(UiNavigator.toOuting(this));
    }

    private void onStoreResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }

        Intent data = result.getData();
        String itemName = data.getStringExtra(StoreActivity.EXTRA_STORE_ITEM_NAME);
        int delta = data.getIntExtra(StoreActivity.EXTRA_STORE_AFFECTION_DELTA, 3);
        handleStoreItemPurchase(itemName, delta);
    }

    private void handleStoreItemPurchase(@Nullable String itemName, int affectionDelta) {
        String finalName = TextUtils.isEmpty(itemName) ? "道具" : itemName;
        int finalDelta = Math.max(1, affectionDelta);
        affectionValue = Math.min(100, affectionValue + finalDelta);
        AffectionHistoryStore.append(this, "商店道具", finalDelta);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        appendRecentChat(RecentChatRole.SYSTEM, "购买并使用了「" + finalName + "」，好感度 +" + finalDelta);
        showToast("购买成功，好感 +" + finalDelta);
    }

    private void onFeedResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }

        Intent data = result.getData();
        String food = data.getStringExtra(FeedActivity.EXTRA_FOOD_NAME);
        int delta = data.getIntExtra(FeedActivity.EXTRA_FOOD_AFFECTION_DELTA, 4);

        affectionValue = Math.min(100, affectionValue + delta);
        AffectionHistoryStore.append(this, "喂食", delta);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        appendRecentChat(RecentChatRole.SYSTEM, "喂食了「" + (food == null ? "食物" : food) + "」，好感度 +" + delta);
        showToast("喂食成功，好感 +" + delta);
    }

    private void onOutingResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }

        Intent data = result.getData();
        String place = data.getStringExtra(OutingActivity.EXTRA_OUTING_PLACE);
        String environment = data.getStringExtra(OutingActivity.EXTRA_OUTING_ENVIRONMENT);
        String imageUrl = data.getStringExtra(OutingActivity.EXTRA_OUTING_IMAGE_URL);
        int delta = data.getIntExtra(OutingActivity.EXTRA_OUTING_AFFECTION_DELTA, 4);

        applyOutingBackground(imageUrl);
        affectionValue = Math.min(100, affectionValue + delta);
        AffectionHistoryStore.append(this, "外出", delta);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        StringBuilder summary = new StringBuilder();
        summary.append("去了「").append(place == null ? "外出地点" : place).append("」");
        if (!TextUtils.isEmpty(environment)) {
            summary.append("，环境：").append(environment);
        }
        summary.append("，好感度 +").append(delta);
        appendRecentChat(RecentChatRole.SYSTEM, summary.toString());
        showToast("外出完成，好感 +" + delta);
    }

    private void applyOutingBackground(@Nullable String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            applyFallbackOutingBackground();
            return;
        }
        SceneImageLoader.loadInto(ivMainBackground, imageUrl, stageBackgrounds[stageBackgroundIndex]);
    }

    private void applyFallbackOutingBackground() {
        stageBackgroundIndex = (stageBackgroundIndex + 1) % stageBackgrounds.length;
        if (ivMainBackground != null) {
            ivMainBackground.setImageResource(stageBackgrounds[stageBackgroundIndex]);
        }
    }

    private OutfitItem findOutfitByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        for (OutfitItem item : outfitCatalog) {
            if (name.equals(item.name)) {
                return item;
            }
        }
        return null;
    }

    private void handleShopSelection(OutfitItem item) {
        handleShopSelectionWithDelta(item, 3);
    }

    private void handleShopSelectionWithDelta(OutfitItem item, int affectionDelta) {
        List<OutfitItem> unlocked = getUnlockedOutfits(activePet.getId());
        if (containsOutfit(unlocked, item.id)) {
            applyOutfit(item);
            showToast("已切换为 " + item.name);
            return;
        }

        unlockOutfit(activePet.getId(), item.id);
        affectionValue = Math.min(100, affectionValue + affectionDelta);
        AffectionHistoryStore.append(this, "商店购买", affectionDelta);
        noticeCount += 1;
        updateAffectionBubble();
        updateNoticeBubble();
        appendRecentChat(RecentChatRole.SYSTEM, "购买了服饰「" + item.name + "」，好感度 +" + affectionDelta);
        applyOutfit(item);
        showToast("购买成功并已换装: " + item.name);
    }

    private void applyOutfit(@NonNull OutfitItem outfitItem) {
        applyExpressionForOutfit(outfitItem.id, null, outfitItem.imageResId, AvatarExpressionResolver.EMOTION_NORMAL);
        tvPetAvatarPlaceholder.setVisibility(View.GONE);
        if (activePet != null) {
            activePet.setAvatar("outfit:" + outfitItem.id);
            activePet.setAppearance(outfitItem.appearanceDesc);
            petStore.updatePet(activePet);
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
            ivPetAvatar.setImageDrawable(null);
            tvPetAvatarPlaceholder.setVisibility(View.GONE);
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
            ivPetAvatar.setImageDrawable(null);
            tvPetAvatarPlaceholder.setVisibility(View.GONE);
            return;
        }

        String avatarValue = activePet.getAvatar();
        if (isCustomAvatar(avatarValue)) {
            applyCustomAvatar(avatarValue);
            return;
        }

        String outfitId = parseOutfitId(activePet.getAvatar());
        String baseImageSource = findDressOutfitImageSourceById(outfitId);
        OutfitItem selected = findOutfitById(outfitId);
        int fallbackResId = R.drawable.ic_outfit;
        if (selected == null) {
            if (TextUtils.isEmpty(baseImageSource)) {
                selected = outfitCatalog.get(0);
                unlockOutfit(activePet.getId(), selected.id);
                activePet.setAvatar("outfit:" + selected.id);
                activePet.setAppearance(selected.appearanceDesc);
                petStore.updatePet(activePet);
                outfitId = selected.id;
                baseImageSource = findDressOutfitImageSourceById(outfitId);
                fallbackResId = selected.imageResId;
            }
        } else {
            fallbackResId = selected.imageResId;
        }

        applyExpressionForOutfit(outfitId, baseImageSource, fallbackResId, AvatarExpressionResolver.EMOTION_NORMAL);
        tvPetAvatarPlaceholder.setVisibility(View.GONE);
    }

    private boolean isCustomAvatar(String avatarValue) {
        return !TextUtils.isEmpty(avatarValue)
                && (avatarValue.startsWith("local:")
                || avatarValue.startsWith("remote:")
                || avatarValue.startsWith("content://")
                || avatarValue.startsWith("file://")
                || avatarValue.startsWith("asset:")
                || avatarValue.startsWith("res:")
                || avatarValue.startsWith("http://")
                || avatarValue.startsWith("https://"));
    }

    private void applyCustomAvatar(@NonNull String avatarValue) {
        SceneImageLoader.loadInto(ivPetAvatar, avatarValue, 0);
        tvPetAvatarPlaceholder.setVisibility(View.GONE);
    }

    private void updateAvatarExpressionByReply(@NonNull String replyText) {
        if (activePet == null) {
            return;
        }
        String avatarValue = activePet.getAvatar();
        if (isCustomAvatar(avatarValue)) {
            return;
        }
        String outfitId = parseOutfitId(avatarValue);
        String baseImageSource = findDressOutfitImageSourceById(outfitId);
        OutfitItem outfit = findOutfitById(outfitId);
        int fallbackResId = R.drawable.ic_outfit;
        if (outfit == null) {
            if (TextUtils.isEmpty(baseImageSource)) {
                outfit = outfitCatalog.get(0);
                outfitId = outfit.id;
                baseImageSource = findDressOutfitImageSourceById(outfitId);
                fallbackResId = outfit.imageResId;
            }
        } else {
            fallbackResId = outfit.imageResId;
        }

        String emotionTag = AvatarExpressionResolver.resolveEmotionTag(replyText);
        applyExpressionForOutfit(outfitId, baseImageSource, fallbackResId, emotionTag);
    }

    private void applyExpressionForOutfit(@NonNull String outfitId,
                                          @Nullable String baseImageSource,
                                          int fallbackResId,
                                          @NonNull String emotionTag) {
        String diffSource = AvatarDiffStore.resolveDiffSource(this, outfitId, emotionTag);
        SceneImageLoader.loadInto(ivPetAvatar, diffSource, baseImageSource, fallbackResId);
    }

    @Nullable
    private String findDressOutfitImageSourceById(@Nullable String outfitId) {
        if (TextUtils.isEmpty(outfitId)) {
            return null;
        }
        List<DressUpOutfitSnapshot> outfits = SPUtils.getList(this, Constants.KEY_DRESS_UP_ITEMS, DressUpOutfitSnapshot.class);
        if (outfits == null || outfits.isEmpty()) {
            return null;
        }
        for (DressUpOutfitSnapshot item : outfits) {
            if (item != null && outfitId.equals(item.id) && !TextUtils.isEmpty(item.imageSource)) {
                return item.imageSource;
            }
        }
        return null;
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

    private static class DressUpOutfitSnapshot {
        public String id;
        public String imageSource;
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
