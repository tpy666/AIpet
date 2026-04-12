package com.example.aipet.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.aipet.R;

/**
 * 帮助页面。
 */
public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setupScreen(getString(R.string.help_title), true);
    }
}
