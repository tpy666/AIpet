package com.example.aipet.util;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 好感度历史记录，支持连续同类操作合并。
 */
public final class AffectionHistoryStore {

    private static final String KEY_AFFECTION_HISTORY = "affection_history";
    private static final int MAX_RECORDS = 200;

    private AffectionHistoryStore() {
    }

    public static void append(@NonNull Context context, @NonNull String actionName, int delta) {
        if (TextUtils.isEmpty(actionName) || delta <= 0) {
            return;
        }
        List<Entry> entries = SPUtils.getList(context, KEY_AFFECTION_HISTORY, Entry.class);
        entries.add(new Entry(actionName, delta, System.currentTimeMillis()));
        if (entries.size() > MAX_RECORDS) {
            entries = new ArrayList<>(entries.subList(entries.size() - MAX_RECORDS, entries.size()));
        }
        SPUtils.putList(context, KEY_AFFECTION_HISTORY, entries);
    }

    @NonNull
    public static List<MergedEntry> getMergedEntries(@NonNull Context context) {
        List<Entry> entries = SPUtils.getList(context, KEY_AFFECTION_HISTORY, Entry.class);
        List<MergedEntry> merged = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry == null || TextUtils.isEmpty(entry.actionName)) {
                continue;
            }
            if (merged.isEmpty()) {
                merged.add(new MergedEntry(entry.actionName, entry.delta, 1));
                continue;
            }
            MergedEntry last = merged.get(merged.size() - 1);
            if (entry.actionName.equals(last.actionName)) {
                last.totalDelta += entry.delta;
                last.count += 1;
            } else {
                merged.add(new MergedEntry(entry.actionName, entry.delta, 1));
            }
        }
        return merged;
    }

    public static class Entry {
        public String actionName;
        public int delta;
        public long timestamp;

        public Entry() {
        }

        Entry(String actionName, int delta, long timestamp) {
            this.actionName = actionName;
            this.delta = delta;
            this.timestamp = timestamp;
        }
    }

    public static class MergedEntry {
        public String actionName;
        public int totalDelta;
        public int count;

        MergedEntry(String actionName, int totalDelta, int count) {
            this.actionName = actionName;
            this.totalDelta = totalDelta;
            this.count = count;
        }
    }
}
