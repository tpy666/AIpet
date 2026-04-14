package com.example.aipet.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 图片源加载器，支持资源、本地 URI、asset 和网络地址。
 */
public final class SceneImageLoader {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    private SceneImageLoader() {
    }

    @Nullable
    public static Bitmap loadBitmap(@NonNull Context context, @NonNull String source) {
        String trimmed = source.trim();
        if (TextUtils.isEmpty(trimmed)) {
            return null;
        }

        try {
            if (trimmed.startsWith("res:")) {
                return loadResourceBitmap(context, trimmed.substring(4));
            }
            if (trimmed.startsWith("asset:")) {
                return loadAssetBitmap(context, trimmed.substring(6));
            }
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return loadRemoteBitmap(trimmed);
            }
            Uri uri = trimmed.startsWith("local:") ? Uri.parse(trimmed.substring(6)) : Uri.parse(trimmed);
            if (!TextUtils.isEmpty(uri.getScheme())) {
                return loadUriBitmap(context, uri);
            }
            return loadFilePathBitmap(trimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void loadInto(@NonNull ImageView imageView,
                                @Nullable String source,
                                int fallbackResId) {
        if (TextUtils.isEmpty(source)) {
            if (fallbackResId != 0) {
                imageView.setImageResource(fallbackResId);
            } else {
                imageView.setImageDrawable(null);
            }
            return;
        }

        Context context = imageView.getContext();
        new Thread(() -> {
            Bitmap bitmap = loadBitmap(context, source);
            imageView.post(() -> {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else if (fallbackResId != 0) {
                    imageView.setImageResource(fallbackResId);
                } else {
                    imageView.setImageDrawable(null);
                }
            });
        }, "scene-image-loader").start();
    }

    public static void loadInto(@NonNull ImageView imageView,
                                @Nullable String primarySource,
                                @Nullable String secondarySource,
                                int fallbackResId) {
        if (TextUtils.isEmpty(primarySource) && TextUtils.isEmpty(secondarySource)) {
            if (fallbackResId != 0) {
                imageView.setImageResource(fallbackResId);
            } else {
                imageView.setImageDrawable(null);
            }
            return;
        }

        Context context = imageView.getContext();
        new Thread(() -> {
            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(primarySource)) {
                bitmap = loadBitmap(context, primarySource);
            }
            if (bitmap == null && !TextUtils.isEmpty(secondarySource)) {
                bitmap = loadBitmap(context, secondarySource);
            }

            Bitmap finalBitmap = bitmap;
            imageView.post(() -> {
                if (finalBitmap != null) {
                    imageView.setImageBitmap(finalBitmap);
                } else if (fallbackResId != 0) {
                    imageView.setImageResource(fallbackResId);
                } else {
                    imageView.setImageDrawable(null);
                }
            });
        }, "scene-image-loader-fallback").start();
    }

    @Nullable
    private static Bitmap loadRemoteBitmap(@NonNull String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            byte[] bytes = response.body().bytes();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    @Nullable
    private static Bitmap loadUriBitmap(@NonNull Context context, @NonNull Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return null;
            }
            return BitmapFactory.decodeStream(inputStream);
        }
    }

    @Nullable
    private static Bitmap loadFilePathBitmap(@NonNull String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    @Nullable
    private static Bitmap loadResourceBitmap(@NonNull Context context, @NonNull String resourceName) {
        String normalized = resourceName;
        if (normalized.startsWith("drawable/")) {
            normalized = normalized.substring("drawable/".length());
        }
        int resId = context.getResources().getIdentifier(normalized, "drawable", context.getPackageName());
        if (resId == 0) {
            resId = context.getResources().getIdentifier(normalized, "mipmap", context.getPackageName());
        }
        if (resId == 0) {
            return null;
        }
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

    @Nullable
    private static Bitmap loadAssetBitmap(@NonNull Context context, @NonNull String assetPath) throws IOException {
        try (InputStream inputStream = context.getAssets().open(assetPath)) {
            return BitmapFactory.decodeStream(inputStream);
        }
    }
}