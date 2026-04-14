package com.example.aipet.util.store;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aipet.util.Constants;
import com.example.aipet.util.SPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 外出背景配置存储。
 */
public class OutingBackgroundStore {

    private final Context appContext;

    public OutingBackgroundStore(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    public List<Entry> loadAll() {
        List<Entry> entries = SPUtils.getList(appContext, Constants.KEY_OUTING_BACKGROUND_LIST, Entry.class);
        if (entries == null) {
            return new ArrayList<>();
        }
        List<Entry> filtered = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry != null && !TextUtils.isEmpty(entry.placeName)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public void saveAll(@NonNull List<Entry> entries) {
        SPUtils.putList(appContext, Constants.KEY_OUTING_BACKGROUND_LIST, entries);
    }

    public void add(@NonNull Entry entry) {
        List<Entry> entries = loadAll();
        entries.add(entry);
        saveAll(entries);
    }

    public boolean removeById(long id) {
        List<Entry> entries = loadAll();
        boolean removed = false;
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry entry = entries.get(i);
            if (entry != null && entry.id == id) {
                entries.remove(i);
                removed = true;
            }
        }
        if (removed) {
            saveAll(entries);
        }
        return removed;
    }

    @Nullable
    public Entry findById(long id) {
        for (Entry entry : loadAll()) {
            if (entry != null && entry.id == id) {
                return entry;
            }
        }
        return null;
    }

    public static class Entry {
        public long id;
        public String placeName;
        public String environment;
        public String imageUrl;
        public int affectionDelta;
        public long createdAt;

        public Entry() {
        }

        public Entry(long id, @NonNull String placeName, @NonNull String environment, @Nullable String imageUrl, int affectionDelta) {
            this.id = id;
            this.placeName = placeName;
            this.environment = environment;
            this.imageUrl = imageUrl == null ? "" : imageUrl;
            this.affectionDelta = affectionDelta;
            this.createdAt = System.currentTimeMillis();
        }
    }
}