package com.example.aipet.ui.avatar;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 头像处理管线：下载网络图片 -> 可选去背 -> 可选上传 -> 本地保存用于展示。
 */
public final class AvatarImagePipeline {

    private static final MediaType PNG_MEDIA_TYPE = MediaType.get("image/png");
    private static final MediaType OCTET_MEDIA_TYPE = MediaType.get("application/octet-stream");
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private AvatarImagePipeline() {
    }

    public interface Callback {
        void onSuccess(@NonNull String localAvatarUri);

        void onFailure(@NonNull String message);
    }

    public static void processRemoteImage(@NonNull Context context,
                                         @NonNull String imageUrl,
                                         @Nullable String uploadEndpoint,
                                         @Nullable String removeBgEndpoint,
                                         boolean autoProcess,
                                         long petId,
                                         @NonNull Callback callback) {
        new Thread(() -> {
            try {
                byte[] originalBytes = downloadBytes(imageUrl);
                if (originalBytes == null || originalBytes.length == 0) {
                    callback.onFailure("无法下载图片");
                    return;
                }

                byte[] finalBytes = originalBytes;
                if (autoProcess && removeBgEndpoint != null && !removeBgEndpoint.trim().isEmpty()) {
                    byte[] processed = removeBackground(originalBytes, removeBgEndpoint.trim());
                    if (processed != null && processed.length > 0) {
                        finalBytes = processed;
                    }
                }

                if (uploadEndpoint != null && !uploadEndpoint.trim().isEmpty()) {
                    uploadAvatar(finalBytes, uploadEndpoint.trim(), imageUrl);
                }

                String uri = saveLocalAvatar(context, petId, finalBytes);
                callback.onSuccess(uri);
            } catch (Exception e) {
                callback.onFailure(e.getMessage() == null ? "头像处理失败" : e.getMessage());
            }
        }, "avatar-image-pipeline").start();
    }

    @Nullable
    private static byte[] downloadBytes(@NonNull String imageUrl) throws IOException {
        Request request = new Request.Builder().url(imageUrl).get().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            return response.body().bytes();
        }
    }

    @Nullable
    private static byte[] removeBackground(@NonNull byte[] inputBytes, @NonNull String removeBgEndpoint) throws IOException {
        RequestBody body = RequestBody.create(inputBytes, OCTET_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(removeBgEndpoint)
                .post(body)
                .addHeader("Content-Type", "application/octet-stream")
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            return response.body().bytes();
        }
    }

    private static void uploadAvatar(@NonNull byte[] avatarBytes,
                                     @NonNull String uploadEndpoint,
                                     @NonNull String sourceName) throws IOException {
        RequestBody fileBody = RequestBody.create(avatarBytes, PNG_MEDIA_TYPE);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", buildFileName(sourceName), fileBody)
                .build();
        Request request = new Request.Builder()
                .url(uploadEndpoint)
                .post(requestBody)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("头像上传失败: HTTP " + response.code());
            }
        }
    }

    @NonNull
    private static String saveLocalAvatar(@NonNull Context context, long petId, @NonNull byte[] avatarBytes) throws IOException {
        File avatarDir = new File(context.getFilesDir(), "avatars");
        if (!avatarDir.exists() && !avatarDir.mkdirs()) {
            throw new IOException("无法创建头像目录");
        }
        File avatarFile = new File(avatarDir, "pet_" + petId + "_avatar.png");
        try (FileOutputStream outputStream = new FileOutputStream(avatarFile)) {
            outputStream.write(avatarBytes);
            outputStream.flush();
        }
        return "local:" + Uri.fromFile(avatarFile).toString();
    }

    @NonNull
    private static String buildFileName(@NonNull String sourceName) {
        String normalized = sourceName.replaceAll("[^A-Za-z0-9]+", "_");
        if (normalized.isEmpty()) {
            normalized = "avatar";
        }
        return normalized + ".png";
    }
}
