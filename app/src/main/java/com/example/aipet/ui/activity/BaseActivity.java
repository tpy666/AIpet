package com.example.aipet.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupScreen(@NonNull String title, boolean showBackButton) {
        setTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
        }
    }

    protected <T extends View> T bind(@IdRes int id) {
        return findViewById(id);
    }

    protected void click(@IdRes int id, View.OnClickListener listener) {
        bind(id).setOnClickListener(listener);
    }

    protected void navigateTo(@NonNull Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    protected void navigateTo(@NonNull Intent intent) {
        startActivity(intent);
    }

    protected void showToast(@NonNull String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(@StringRes int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}