package com.example.aipet.ui.activity;

import android.os.Bundle;

import com.example.aipet.R;
import com.example.aipet.ui.navigation.UiNavigator;

/**
 * 首页 - 主菜单页面
 * 包含导航至各个功能页面的按钮
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupScreen("AI 宠物助手", false);

        initViews();
    }

    private void initViews() {
        click(R.id.btn_create_pet, v -> navigateTo(UiNavigator.toCreatePet(this)));
        click(R.id.btn_pet_list, v -> navigateTo(UiNavigator.toPetList(this)));
        click(R.id.btn_chat, v -> navigateTo(UiNavigator.toChat(this)));
        click(R.id.btn_settings, v -> navigateTo(UiNavigator.toSettings(this)));
    }
}
