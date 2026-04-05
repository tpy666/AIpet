package com.example.aipet.ui.navigation;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.aipet.data.model.Pet;
import com.example.aipet.ui.activity.ChatActivity;
import com.example.aipet.ui.activity.ChatLogViewerActivity;
import com.example.aipet.ui.activity.CreatePetActivity;
import com.example.aipet.ui.activity.ErrorLogViewerActivity;
import com.example.aipet.ui.activity.MainActivity;
import com.example.aipet.ui.activity.PetCardListActivity;
import com.example.aipet.ui.activity.SettingsActivity;

/**
 * UI 导航总引导
 *
 * 集中管理页面跳转，避免 Activity 内部散落硬编码 Intent。
 */
public final class UiNavigator {

    public static final String EXTRA_PET = "extra_pet";

    private UiNavigator() {
    }

    public static Intent toMain(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    public static Intent toCreatePet(@NonNull Context context) {
        return new Intent(context, CreatePetActivity.class);
    }

    public static Intent toPetList(@NonNull Context context) {
        return new Intent(context, PetCardListActivity.class);
    }

    public static Intent toChat(@NonNull Context context) {
        return new Intent(context, ChatActivity.class);
    }

    public static Intent toChat(@NonNull Context context, @NonNull Pet pet) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_PET, pet);
        return intent;
    }

    public static Intent toSettings(@NonNull Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    public static Intent toChatLogs(@NonNull Context context) {
        return new Intent(context, ChatLogViewerActivity.class);
    }

    public static Intent toErrorLogs(@NonNull Context context) {
        return new Intent(context, ErrorLogViewerActivity.class);
    }
}
