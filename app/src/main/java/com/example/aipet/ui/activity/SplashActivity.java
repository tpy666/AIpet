package com.example.aipet.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.aipet.R;
import com.example.aipet.network.NetworkConfigBootstrap;
import com.example.aipet.ui.navigation.UiNavigator;
import com.example.aipet.util.Constants;

/**
 * 启动页 - 应用入口
 * 延迟 2 秒后自动跳转到首页
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 启动时先同步一次持久化网络配置，避免回落默认本地后端
        NetworkConfigBootstrap.syncFromStorage(this);

        // 延迟跳转到首页
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateTo(UiNavigator.toMain(this));
            finish();
        }, Constants.SPLASH_DELAY);
    }
}
