package com.example.aipet.ui.activity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.aipet.R;
import com.example.aipet.util.AffectionHistoryStore;

import java.util.List;

/**
 * 好感度页：展示不同操作带来的好感提升，连续相同操作合并展示。
 */
public class AffectionActivity extends BaseActivity {

    private TextView tvAffectionValue;
    private LinearLayout layoutRecords;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affection);
        setupScreen("好感度", true);

        tvAffectionValue = bind(R.id.tv_affection_value);
        layoutRecords = bind(R.id.layout_affection_records);

        int affectionValue = getIntent().getIntExtra("affection_value", 50);
        tvAffectionValue.setText("当前好感度：" + affectionValue);

        renderRecords(AffectionHistoryStore.getMergedEntries(this));
    }

    private void renderRecords(List<AffectionHistoryStore.MergedEntry> entries) {
        layoutRecords.removeAllViews();

        if (entries.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("暂无记录，先去互动吧");
            empty.setTextSize(13f);
            empty.setTextColor(getColor(R.color.text_secondary));
            layoutRecords.addView(empty);
            return;
        }

        for (AffectionHistoryStore.MergedEntry entry : entries) {
            android.view.View row = getLayoutInflater().inflate(R.layout.item_affection_record, layoutRecords, false);
            TextView tvTitle = row.findViewById(R.id.tv_affection_action);
            TextView tvDelta = row.findViewById(R.id.tv_affection_delta);
            TextView tvCount = row.findViewById(R.id.tv_affection_count);

            tvTitle.setText(entry.actionName);
            tvDelta.setText("+" + entry.totalDelta);
            tvCount.setText("连续 " + entry.count + " 次");
            layoutRecords.addView(row);
        }
    }
}
